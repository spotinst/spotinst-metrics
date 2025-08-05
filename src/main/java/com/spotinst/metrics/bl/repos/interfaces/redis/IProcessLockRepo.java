package com.spotinst.metrics.bl.repos.interfaces.redis;


import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;

/**
 * Created by ohadmuchnik on 07/12/2016.
 */
public interface IProcessLockRepo {

    RepoGenericResponse<Boolean> acquireLock(String key, String value, Integer expire);

    RepoGenericResponse<Boolean> setLock(String key, String value, Integer expire);

    RepoGenericResponse<String> getLock(String key);

    RepoGenericResponse<Boolean> deleteLock(String key);
}
