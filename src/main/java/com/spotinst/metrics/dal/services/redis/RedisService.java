package com.spotinst.metrics.dal.services.redis;

import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.commons.configuration.RedisConfig;
import com.spotinst.metrics.commons.utils.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.SetParams;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by dvirkatz on 18/12/2016.
 */
public class RedisService {
    private static final String        REDIS_SET_SUCCESS_STATUS = "OK";
    private              JedisPool     jedisPool                = null;
    private static final Logger        LOGGER                   = LoggerFactory.getLogger(RedisService.class);
    private static final Object        LOCK_OBJ                 = new Object();
    private static final Integer       WARM_UP_THREAD_POOL_SIZE = 10;
    private              RedisCounters redisCounters;

    // region SINGLETON
    private static RedisService instance;

    // Lazy Initialization (If required then only)
    public static RedisService getInstance() {
        if (instance == null) {
            // Thread Safe. Might be costly operation in some case
            synchronized (LOCK_OBJ) {
                if (instance == null) {
                    instance = new RedisService();
                }
            }
        }

        return instance;
    }
    //endregion

    //region constructor
    private RedisService() {
        redisCounters = new RedisCounters();

        try {
            RedisConfig redisConfig = MetricsAppContext.getInstance().getConfiguration().getRedisConfiguration();

            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(redisConfig.getPoolMaxTotal());
            config.setMaxIdle(redisConfig.getPoolMaxIdle());
            config.setMinIdle(redisConfig.getPoolMinIdle());
            config.setMaxWait(Duration.ofMillis(redisConfig.getPoolMaxWaitMillis()));

            if (redisConfig.getTestOnBorrow() != null) {
                config.setTestOnBorrow(redisConfig.getTestOnBorrow());
            }

            if (redisConfig.getTestOnReturn() != null) {
                config.setTestOnReturn(redisConfig.getTestOnReturn());
            }

            if (redisConfig.getTestOnCreate() != null) {
                config.setTestOnCreate(redisConfig.getTestOnCreate());
            }

            if (redisConfig.getTestWhileIdle() != null) {
                config.setTestWhileIdle(redisConfig.getTestWhileIdle());
            }

            if (redisConfig.getNumTestsPerEvictionRun() != null) {
                config.setNumTestsPerEvictionRun(redisConfig.getNumTestsPerEvictionRun());
            }

            if (redisConfig.getTimeBetweenEvictionRunsMillis() != null) {
                config.setTimeBetweenEvictionRuns( Duration.ofMillis(redisConfig.getTimeBetweenEvictionRunsMillis()) );
            }

            if (redisConfig.getMinEvictableIdleTimeMillis() != null) {
                config.setMinEvictableIdleDuration( Duration.ofMillis(redisConfig.getMinEvictableIdleTimeMillis()));
            }

            String password = redisConfig.getPassword();
            if (password == null) {
                jedisPool =
                        new JedisPool(config, redisConfig.getHost(), redisConfig.getPort(), redisConfig.getTimeout());
            }
            else {
                jedisPool =
                        new JedisPool(config, redisConfig.getHost(), redisConfig.getPort(), redisConfig.getTimeout(),
                                      redisConfig.getPassword());
            }

        }
        catch (JedisConnectionException ex) {
            LOGGER.error("Could not establish Redis connection !", ex);
        }
    }
    //endregion

    //region public methods
    public void warmUpConnections() {
        if (jedisPool != null) {

            RedisConfig redisConfig = MetricsAppContext.getInstance().getConfiguration().getRedisConfiguration();
            Integer     poolMinIdle = redisConfig.getPoolMinIdle();

            ExecutorService executor                = Executors.newFixedThreadPool(WARM_UP_THREAD_POOL_SIZE);
            Queue<Jedis>    createdResources        = new ConcurrentLinkedQueue<>();
            AtomicInteger   createdResourcesCounter = new AtomicInteger(0);
            AtomicBoolean   shouldProceed           = new AtomicBoolean(true);

            for (int i = 0; i < poolMinIdle; i++) {

                executor.execute(() -> {
                    if (shouldProceed.get()) {
                        try {
                            Jedis resource = jedisPool.getResource();
                            if (resource != null) {
                                createdResources.add(resource);
                                int numOfCreatedResources = createdResourcesCounter.incrementAndGet();
                                if (numOfCreatedResources % 10 == 0) {
                                    LOGGER.info(String.format("Loaded %s Redis connections out of: %s",
                                                              numOfCreatedResources, poolMinIdle));
                                }
                            }
                            else {
                                shouldProceed.set(false);
                            }
                        }
                        catch (Exception ex) {
                            LOGGER.warn(String.format("Failed to init Redis resource. Exception: %s", ex), ex);
                            shouldProceed.set(false);
                        }
                    }
                });
            }

            // Wait for all tasks to end.
            ExecutorUtils.shutdownAndAwaitTermination(executor);

            if (createdResources.size() > 0) {
                LOGGER.info(String.format("Loaded total of: %s Redis connections", createdResources.size()));
            }

            Jedis resource;
            while ((resource = createdResources.poll()) != null) {
                try {
                    resource.close();
                }
                catch (Exception ex) {
                    LOGGER.error(String.format("Failed to close Redis resource. Exception: %s", ex), ex);
                }
            }
        }
        else {
            LOGGER.error("Could not warm up Redis connections, pool isn't initialized");
        }
    }

    public Boolean setValue(String key, String value, Integer expire) throws RedisException {
        Boolean retVal;
        Jedis   resource  = null;
        Date    startTime = MetricsAppContext.getInstance().getSystemClock().getCurrentTime();
        redisCounters.totalRequests.incrementAndGet();

        try {
            LOGGER.debug(String.format("setting value to redis - key: '%s' value:'%s' ", key, value));
            resource = jedisPool.getResource();

            String result;
            if (expire != null) {
                result = resource.setex(key, expire, value);
            }
            else {
                result = resource.set(key, value);
            }

            if (result.equals(REDIS_SET_SUCCESS_STATUS)) {
                retVal = true;
            }
            else {
                retVal = false;
            }
        }
        catch (Exception ex) {
            redisCounters.failedRequests.incrementAndGet();
            LOGGER.warn(String.format("Failed to set value via Redis : '%s', for key : '%s'. Exception: %s", value, key,
                                      ex), ex);
            throw new RedisException(ex.getMessage());
        }
        finally {

            if (resource != null) {
                resource.close();
            }

            Date endTime  = MetricsAppContext.getInstance().getSystemClock().getCurrentTime();
            long duration = endTime.getTime() - startTime.getTime();
            redisCounters.totalRequestsDurationInMs.addAndGet(duration);
        }

        return retVal;
    }

    public String getValue(String key) throws RedisException {
        String retVal;
        Jedis  resource = null;

        Date startTime = MetricsAppContext.getInstance().getSystemClock().getCurrentTime();
        redisCounters.totalRequests.incrementAndGet();

        try {
            resource = jedisPool.getResource();
            retVal = resource.get(key);
            LOGGER.debug(String.format("get value for key: '%s' returned '%s' ", key, retVal));
        }
        catch (Exception ex) {
            redisCounters.failedRequests.incrementAndGet();
            LOGGER.warn(String.format("Failed to get value for key via Redis : '%s'. Exception: %s", key, ex), ex);
            throw new RedisException(ex.getMessage());
        }
        finally {
            if (resource != null) {
                resource.close();
            }

            Date endTime  = MetricsAppContext.getInstance().getSystemClock().getCurrentTime();
            long duration = endTime.getTime() - startTime.getTime();
            redisCounters.totalRequestsDurationInMs.addAndGet(duration);
        }

        return retVal;
    }

    public Boolean deleteKey(String key) throws RedisException {
        Boolean retVal;
        Jedis   resource = null;

        Date startTime = MetricsAppContext.getInstance().getSystemClock().getCurrentTime();
        redisCounters.totalRequests.incrementAndGet();

        try {
            resource = jedisPool.getResource();
            LOGGER.debug(String.format("delete redis key: '%s' ", key));
            Long numOfDeletedKeys = resource.del(key);

            if (numOfDeletedKeys != null && numOfDeletedKeys > 0) {
                retVal = true;
            }
            else {
                retVal = false;
            }
        }
        catch (Exception ex) {
            redisCounters.failedRequests.incrementAndGet();
            LOGGER.warn(String.format("Failed to delete key via Redis : '%s'. Exception: %s", key, ex), ex);
            throw new RedisException(ex.getMessage());
        }
        finally {
            if (resource != null) {
                resource.close();
            }

            Date endTime  = MetricsAppContext.getInstance().getSystemClock().getCurrentTime();
            long duration = endTime.getTime() - startTime.getTime();
            redisCounters.totalRequestsDurationInMs.addAndGet(duration);
        }

        return retVal;
    }

    public Boolean setIfNotExist(String key, String value, Integer expire) throws RedisException {
        Boolean retVal;
        Jedis   resource  = null;
        Date    startTime = MetricsAppContext.getInstance().getSystemClock().getCurrentTime();
        redisCounters.totalRequests.incrementAndGet();

        try {
            LOGGER.debug(String.format("setting value to redis if not exist - key: '%s' value:'%s' ", key, value));
            resource = jedisPool.getResource();

            String result;
            SetParams params = new SetParams();
            if (expire != null) {
                result = resource.set(key, value, params.nx().ex(expire));
            }
            else {
                result = resource.set(key, value, params.nx());
            }

            if (Objects.equals(result, REDIS_SET_SUCCESS_STATUS)) {
                retVal = true;
            }
            else {
                retVal = false;
            }
        }
        catch (Exception ex) {
            redisCounters.failedRequests.incrementAndGet();
            LOGGER.warn(String.format("Failed to set value via Redis if not exist: '%s', for key : '%s'. Exception: %s",
                                      value, key, ex), ex);
            throw new RedisException(ex.getMessage());
        }
        finally {

            if (resource != null) {
                resource.close();
            }

            Date endTime  = MetricsAppContext.getInstance().getSystemClock().getCurrentTime();
            long duration = endTime.getTime() - startTime.getTime();
            redisCounters.totalRequestsDurationInMs.addAndGet(duration);
        }

        return retVal;
    }

    public Map<String, String> getMap(String key) throws RedisException {
        Map<String, String> retVal;

        Jedis resource = null;

        try {
            resource = jedisPool.getResource();
            retVal = resource.hgetAll(key);
            LOGGER.debug(String.format("got value for map: '%s'", key));
        }
        catch (Exception ex) {
            LOGGER.warn(String.format("Failed to get value for map via Redis : '%s'", key), ex);
            throw new RedisException(ex.getMessage());
        }
        finally {
            if (resource != null) {
                resource.close();
            }
        }

        return retVal;
    }

    public boolean deleteFieldsFromMap(String mapKey, List<String> fields) throws RedisException {
        Jedis resource = null;

        try {
            resource = jedisPool.getResource();

            String[] fieldsArray = new String[fields.size()];
            fieldsArray = fields.toArray(fieldsArray);
            Long result = resource.hdel(mapKey, fieldsArray);
            LOGGER.debug(String.format("Deleted %s fields from map: '%s'", result, mapKey));
        }
        catch (Exception ex) {
            LOGGER.warn(String.format("Failed to delete fields from map via Redis : '%s'", mapKey), ex);
            throw new RedisException(ex.getMessage());
        }
        finally {
            if (resource != null) {
                resource.close();
            }
        }

        return true;
    }
    //endregion

    //region RedisCounters
    private static class RedisCounters {
        private AtomicLong totalRequests;
        private AtomicLong totalRequestsDurationInMs;
        private AtomicLong failedRequests;

        private RedisCounters() {
            totalRequests = new AtomicLong(0);
            failedRequests = new AtomicLong(0);
            totalRequestsDurationInMs = new AtomicLong(0);
        }
    }

    public void logCounters() {
        long totalRequests             = redisCounters.totalRequests.getAndSet(0);
        long totalRequestsDurationInMs = redisCounters.totalRequestsDurationInMs.getAndSet(0);
        long failedRequests            = redisCounters.failedRequests.getAndSet(0);

        if (totalRequests > 0) {
            long avgRequestsDurationInMs = totalRequestsDurationInMs / totalRequests;

            LOGGER.info(String.format("RedisCounters: totalRequests: %s, avgDurationInMs: %s, failedRequests: %s",
                                      totalRequests, avgRequestsDurationInMs, failedRequests));
        }
    }
    //endregion
}
