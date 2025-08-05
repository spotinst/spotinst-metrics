package com.spotinst.metrics.bl.repos.interfaces.redis;


import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;

import java.util.List;
import java.util.Map;

/**
 * Created by ohadmuchnik on 07/12/2016.
 */
public interface IRedisRepo {

    RepoGenericResponse<Boolean> setKey(String key, String value, Integer ttl);

    RepoGenericResponse<Map<String, String>> getMap(String key);

    RepoGenericResponse<Boolean> deleteFieldsFromMap(String mapKey, List<String> fields);
}
