package com.spotinst.service.bl.repos.impl.redis;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.service.bl.repos.interfaces.redis.IProcessLockRepo;
import com.spotinst.service.dal.services.redis.RedisException;
import com.spotinst.service.dal.services.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohadmuchnik on 07/12/2016.
 */
public class ProcessLockRepo implements IProcessLockRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessLockRepo.class);

    @Override
    public RepoGenericResponse<Boolean> acquireLock(String key, String value, Integer expire) {
        RepoGenericResponse<Boolean> retVal;
        try {
            Boolean setSucceed = RedisService.getInstance().setIfNotExist(key, value, expire);
            retVal = new RepoGenericResponse<>(setSucceed);
        }
        catch (RedisException ex) {
            LOGGER.error(
                    String.format("Exception while trying to acquire lock for key : '%s' with value '%s'", key, value));
            retVal = new RepoGenericResponse<>(false);
        }
        return retVal;
    }

    @Override
    public RepoGenericResponse<Boolean> deleteLock(String key) {

        RepoGenericResponse<Boolean> retVal;
        try {
            Boolean deleteSucceed = RedisService.getInstance().deleteKey(key);
            retVal = new RepoGenericResponse<>(deleteSucceed);
        }
        catch (RedisException ex) {
            LOGGER.error(String.format("Failed to delete lock : '%s'", key));
            retVal = new RepoGenericResponse<>(false);
        }
        return retVal;
    }

    @Override
    public RepoGenericResponse<Boolean> setLock(String key, String value, Integer expire) {
        RepoGenericResponse<Boolean> retVal;
        try {
            Boolean setSucceed = RedisService.getInstance().setValue(key, value, expire);
            retVal = new RepoGenericResponse<>(setSucceed);
        }
        catch (RedisException ex) {
            LOGGER.error(String.format("Failed to set lock for key : '%s' with value '%s'", key, value));
            retVal = new RepoGenericResponse<>(false);
        }
        return retVal;
    }

    @Override
    public RepoGenericResponse<String> getLock(String key) {
        RepoGenericResponse<String> retVal;
        try {
            String lockValue = RedisService.getInstance().getValue(key);
            retVal = new RepoGenericResponse<>(lockValue);
        }
        catch (RedisException ex) {
            LOGGER.error(String.format("Failed to get lock for key : '%s'", key));
            retVal = new RepoGenericResponse<>(false);
        }
        return retVal;
    }
}
