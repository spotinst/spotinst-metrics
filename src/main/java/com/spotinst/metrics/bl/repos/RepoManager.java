package com.spotinst.metrics.bl.repos;

import com.spotinst.dropwizard.bl.repo.BaseRepoManager;
import com.spotinst.metrics.bl.repos.impl.MetricRepo;
import com.spotinst.metrics.bl.repos.impl.PercentilesRepo;
import com.spotinst.metrics.bl.repos.impl.redis.ProcessLockRepo;
import com.spotinst.metrics.bl.repos.interfaces.IMetricRepo;
import com.spotinst.metrics.bl.repos.interfaces.IPercentilesRepo;
import com.spotinst.metrics.bl.repos.interfaces.redis.IProcessLockRepo;

public class RepoManager extends BaseRepoManager {
    //region Members
    public static IMetricRepo      metricRepo;
    public static IPercentilesRepo percentilesRepo;
    public static IProcessLockRepo processLockRepo;
    //endregion

    //region Constructor
    public RepoManager() {
    }
    //endregion

    //region Singleton
    static {
        initRepositories();
    }

    //endregion

    //region Public Methods
    private static void initRepositories() {
        processLockRepo = new ProcessLockRepo();
        metricRepo = new MetricRepo();
        percentilesRepo = new PercentilesRepo();
    }
    //endregion
}
