package com.spotinst.service.bl.repos;

import com.spotinst.dropwizard.bl.repo.BaseRepoManager;
import com.spotinst.service.bl.repos.dummy.standard.DummyRepo;
import com.spotinst.service.bl.repos.impl.dbJava.core.OrganizationRepo;
import com.spotinst.service.bl.repos.impl.redis.ProcessLockRepo;
import com.spotinst.service.bl.repos.interfaces.dbJava.core.IOrganizationRepo;
import com.spotinst.service.bl.repos.interfaces.redis.IProcessLockRepo;

/**
 * Every repository class name should comply to the `IxxxRepo` format
 * It's property name should be the name of the entity it represents
 */
public class SampleRepoManager extends BaseRepoManager {
    //region Members
    public static IOrganizationRepo Organization;
    public static IProcessLockRepo  processLockRepo;
    //Dummy
    public static DummyRepo        Dummy;
    //endregion

    static {
        initRepositories();
    }

    private static void initRepositories() {
        Organization = new OrganizationRepo();
        processLockRepo = new ProcessLockRepo();
        Dummy = new DummyRepo();
    }
}
