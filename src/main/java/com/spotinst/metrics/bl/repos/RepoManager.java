package com.spotinst.metrics.bl.repos;

import com.spotinst.dropwizard.bl.repo.BaseRepoManager;
import com.spotinst.metrics.bl.repos.impl.MetricRepo;
import com.spotinst.metrics.bl.repos.impl.PercentilesRepo;
import com.spotinst.metrics.bl.repos.interfaces.IMetricRepo;
import com.spotinst.metrics.bl.repos.interfaces.IPercentilesRepo;

public class RepoManager extends BaseRepoManager {
    //region Members
    public static IMetricRepo      metricRepo;
    public static IPercentilesRepo percentilesRepo;
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
        metricRepo = new MetricRepo();
        percentilesRepo = new PercentilesRepo();
    }
    //endregion
}
