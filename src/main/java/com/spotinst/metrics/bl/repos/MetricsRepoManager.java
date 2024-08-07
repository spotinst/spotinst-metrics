package com.spotinst.metrics.bl.repos;

import com.spotinst.dropwizard.bl.repo.BaseRepoManager;
import com.spotinst.metrics.bl.repos.impl.MetricRepo;
import com.spotinst.metrics.bl.repos.impl.dbJava.core.OrganizationRepo;
import com.spotinst.metrics.bl.repos.impl.redis.ProcessLockRepo;
import com.spotinst.metrics.bl.repos.interfaces.IMetricRepo;
import com.spotinst.metrics.bl.repos.interfaces.dbJava.core.IOrganizationRepo;
import com.spotinst.metrics.bl.repos.interfaces.redis.IProcessLockRepo;

/**
 * Every repository class name should comply to the `IxxxRepo` format
 * It's property name should be the name of the entity it represents
 */
public class MetricsRepoManager extends BaseRepoManager {
    //region Members
    private static MetricsRepoManager instance = new MetricsRepoManager();
    public static  IMetricRepo        metricRepo;
    public static  IOrganizationRepo  Organization; //TODO Tal: think if needed
    public static  IProcessLockRepo   processLockRepo; //TODO Tal: think if needed
    //endregion

    static {
        initRepositories();
    }

    private static void initRepositories() {
        Organization = new OrganizationRepo();
        processLockRepo = new ProcessLockRepo();
        metricRepo = new MetricRepo();
    }

    //region Singleton
    public static MetricsRepoManager getInstance() {
        return instance;
    }
    //endregion
}
