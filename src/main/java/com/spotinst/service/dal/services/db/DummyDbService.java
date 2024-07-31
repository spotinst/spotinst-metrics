package com.spotinst.service.dal.services.db;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.dropwizard.common.service.BaseSpotinstService;
import com.spotinst.service.SampleAppContext;
import com.spotinst.service.commons.configuration.ServicesConfig;
import com.spotinst.service.dal.models.db.dummy.DbDummy;
import com.spotinst.service.dal.models.db.dummy.DummyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DummyDbService extends BaseSpotinstService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyDbService.class);

    private static String SPOTINST_DB_JAVA_SERVICE;

    static {
        ServicesConfig services = SampleAppContext.getInstance().getConfiguration().getServices();
        SPOTINST_DB_JAVA_SERVICE = services.getDbServiceUrl();
    }

    //region Dummy
    public static DbDummy createDummy(DbDummy dbDummy) throws DalException {
        return null;
    }

    public static Boolean updateDummy(String dummyId, DbDummy dbDummy) throws DalException {
        return null;
    }

    public static DbDummy getDummy(String dummyId) throws DalException {
        return null;
    }

    public static List<DbDummy> getAllDummies(DummyFilter filter) throws DalException {
        return null;
    }

    public static Boolean deleteDummy(String dummyId) throws DalException {
        return null;
    }
    //endregion
}
