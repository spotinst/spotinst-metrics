package com.spotinst.service.dal.services.db.infra;

import com.spotinst.dropwizard.bl.repo.IEntityFilter;
import com.spotinst.dropwizard.bl.service.IGenericService;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;

import java.util.List;

/**
 * Created by zachi.nachshon on 7/15/17.
 */
public interface IBulkGenericService<Id, DbEntity, Filter extends IEntityFilter, BulkRequest> extends IGenericService<Id, DbEntity, Filter> {
    boolean createBulk(BulkRequest request) throws DalException;

    boolean updateBulk(BulkRequest request) throws DalException;

    boolean deleteBulk(List<Id> ids) throws DalException;

    void asyncCreateBulk(BulkRequest request) throws DalException;

    void asyncUpdateBulk(BulkRequest request) throws DalException;

    void asyncDeleteBulk(List<Id> ids) throws DalException;
}
