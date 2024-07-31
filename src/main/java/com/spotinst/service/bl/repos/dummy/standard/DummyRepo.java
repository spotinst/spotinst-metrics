package com.spotinst.service.bl.repos.dummy.standard;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.ExceptionHelper;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.service.bl.model.dummy.BlDummy;
import com.spotinst.service.commons.converters.Converters;
import com.spotinst.service.dal.models.db.dummy.DbDummy;
import com.spotinst.service.dal.models.db.dummy.DummyFilter;
import com.spotinst.service.dal.services.db.DummyDbService;

import java.util.LinkedList;
import java.util.List;

/**
 * This is the full repository implementation option, usually gets duplicated for every entity.
 *
 * Created by zachi.nachshon on 2/8/17.
 */
public class DummyRepo implements IDummyRepo {

    @Override
    public RepoGenericResponse<BlDummy> create(BlDummy blDummy) {
        RepoGenericResponse<BlDummy> retVal;

        try {
            BlDummy result       = null;
            DbDummy dbDummy      = Converters.Dummy.toDb(blDummy);
            DbDummy createdDummy = DummyDbService.createDummy(dbDummy);

            if (createdDummy != null) {
                result = Converters.Dummy.toBl(createdDummy);
            }

            retVal = new RepoGenericResponse<>(result);
        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }

    @Override
    public RepoGenericResponse<Boolean> update(String dummyId, BlDummy blDummy) {

        RepoGenericResponse<Boolean> retVal;

        try {
            DbDummy dbDummy = Converters.Dummy.toDb(blDummy);
            Boolean success = DummyDbService.updateDummy(dummyId, dbDummy);
            retVal = new RepoGenericResponse<>(success);
        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }

    @Override
    public RepoGenericResponse<BlDummy> get(String dummyId) {

        RepoGenericResponse<BlDummy> retVal;

        try {
            BlDummy blDummy = null;
            DbDummy dbDummy = DummyDbService.getDummy(dummyId);

            if (dbDummy != null) {
                blDummy = Converters.Dummy.toBl(dbDummy);
            }

            retVal = new RepoGenericResponse<>(blDummy);
        }
        catch (DalException e) {
            retVal = ExceptionHelper.handleDalException(e);
        }

        return retVal;
    }

    @Override
    public RepoGenericResponse<List<BlDummy>> getAll(DummyFilter filter) {
        RepoGenericResponse<List<BlDummy>> retVal;

        try {
            List<BlDummy> blDummies = new LinkedList<>();
            List<DbDummy> dbDummies = DummyDbService.getAllDummies(filter);

            if(dbDummies != null) {
                for (DbDummy DbDummy : dbDummies) {
                    BlDummy blDummy = Converters.Dummy.toBl(DbDummy);
                    blDummies.add(blDummy);
                }
            }

            retVal = new RepoGenericResponse<>(blDummies);
        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }

    @Override
    public RepoGenericResponse<Boolean> delete(String dummyId) {
        RepoGenericResponse<Boolean> retVal;

        try {
            Boolean succeed = DummyDbService.deleteDummy(dummyId);
            retVal = new RepoGenericResponse<>(succeed);
        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }
}
