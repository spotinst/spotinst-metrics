package com.spotinst.metrics.bl.repos;

import com.spotinst.dropwizard.bl.repo.BaseRepoManager;
import com.spotinst.metrics.bl.repos.impl.MetricRepo;
import com.spotinst.metrics.bl.repos.impl.PercentilesRepo;
import com.spotinst.metrics.bl.repos.impl.redis.ProcessLockRepo;
import com.spotinst.metrics.bl.repos.interfaces.IMetricRepo;
import com.spotinst.metrics.bl.repos.interfaces.IPercentilesRepo;
import com.spotinst.metrics.bl.repos.interfaces.redis.IProcessLockRepo;
import lombok.Getter;

/**
 * Every repository class name should comply to the `IxxxRepo` format
 * It's property name should be the name of the entity it represents
 */
public class RepoManager extends BaseRepoManager {
    //region Singleton
    //region Members
    @Getter
    private static RepoManager       instance = new RepoManager();
    public static  IMetricRepo       metricRepo;
    public static  IPercentilesRepo  percentilesRepo;
    public static  IProcessLockRepo  processLockRepo; //TODO Tal: think if needed
    //endregion

    static {
        initRepositories();
    }

    private static void initRepositories() {
        processLockRepo = new ProcessLockRepo();
        metricRepo = new MetricRepo();
        percentilesRepo = new PercentilesRepo();
    }

    //endregion
}
