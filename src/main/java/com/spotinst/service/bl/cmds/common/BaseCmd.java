package com.spotinst.service.bl.cmds.common;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.context.RequestsContextManager;
import com.spotinst.service.SampleAppContext;
import com.spotinst.service.bl.repos.SampleRepoManager;
import com.spotinst.service.bl.repos.interfaces.redis.IProcessLockRepo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by snir on 14/02/16.
 */
public class BaseCmd<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCmd.class);

    private static final String DEFAULT_SERVICE_NAME = "sample";

    //region Members
    private T       result;
    private String  message;
    private String  traceId;
    private boolean isLockAcquired;
    private long    startTime;
    private String  serviceName;
    //endregion

    //region Constructors
    public BaseCmd() {
        this.traceId = "opid-" + UUID.randomUUID();
        this.startTime = System.nanoTime();
        this.serviceName = SampleAppContext.getInstance().getEnvironment().getName();

        if (StringUtils.isEmpty(serviceName)) {
            LOGGER.warn(String.format("Service name not set in main application file, using default service name %s",
                                      DEFAULT_SERVICE_NAME));
            this.serviceName = DEFAULT_SERVICE_NAME;
        }
    }
    //endregion

    protected boolean acquireLockIfNotLocked(String processName, String resourceId, Integer lockExpiryInSec) {
        boolean retVal = true;

        IProcessLockRepo processLockRepo = SampleRepoManager.processLockRepo;
        String           processKey      = buildLockKey(processName, resourceId);

        RepoGenericResponse<Boolean> redisResponse =
                processLockRepo.acquireLock(processKey, "Running", lockExpiryInSec);

        if (redisResponse.isRequestSucceed()) {
            Boolean isSetSucceeded = redisResponse.getValue();

            if (Objects.equals(isSetSucceeded, false)) {
                // Other process is running
                LOGGER.info(
                        String.format("%s process is already running for resource: %s. Skipping this run", processName,
                                      resourceId));
                retVal = false;
            }
            else {
                isLockAcquired = true;
            }
        }
        else {
            LOGGER.error(String.format("a setLock for key (%s) to redis have failed! => will not run", processKey));
            retVal = false;
        }

        return retVal;
    }

    protected void releaseLock(String processName, String resourceId) {
        if (isLockAcquired) {
            IProcessLockRepo             processLockRepo = SampleRepoManager.processLockRepo;
            String                       processKey      = buildLockKey(processName, resourceId);
            RepoGenericResponse<Boolean> deleteResponse  = processLockRepo.deleteLock(processKey);

            if (deleteResponse.isRequestSucceed()) {
                Boolean deleteSuccess = deleteResponse.getValue();

                if (deleteSuccess) {
                    LOGGER.info(String.format("Successfully deleted redis key %s and released lock for process %s",
                                              processKey, processName));
                }
                else {
                    LOGGER.warn(String.format("Failed to delete key %s and release lock for process %s", processKey,
                                              processName));
                }
            }
            else {
                LOGGER.info(String.format("Error while trying to delete redis key %s to release process %s. Errors: %s",
                                          processKey, processName, deleteResponse.getDalErrors()));
            }
        }
    }
    //region Getters & Setters

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    //endregion

    //region private methods
    private String buildLockKey(String processName, String resourceId) {
        String organization = RequestsContextManager.getContext().getOrganizationId();
        String accountId    = RequestsContextManager.getContext().getAccountId();
        String lockKey;

        if (accountId != null) {
            lockKey = String.format("%s:%s:%s:%s:%s", serviceName, organization, accountId, resourceId, processName);
        }
        else {
            lockKey = String.format("%s:%s:%s:%s", serviceName, organization, resourceId, processName);
        }

        return lockKey;
    }

    public void printElapsedTime() {
        long difference = System.nanoTime() - this.startTime;
        LOGGER.debug(String.format("Total execution time for %s: %d min, %d sec", this.getClass().getName(),
                                   TimeUnit.NANOSECONDS.toMinutes(difference),
                                   TimeUnit.NANOSECONDS.toSeconds(difference) -
                                   TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(difference))));
    }
    //endregion
}
