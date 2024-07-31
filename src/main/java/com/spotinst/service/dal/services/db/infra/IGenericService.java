package com.spotinst.service.dal.services.db.infra;

import com.spotinst.dropwizard.bl.repo.IEntityFilter;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;

import java.util.List;

/**
 * Created by zachi.nachshon on 7/15/17.
 */
public interface IGenericService<Id, DbEntity, Filter extends IEntityFilter> {
    DbEntity get(Id id) throws DalException;
    List<DbEntity> getAll(Filter filter) throws DalException;
    DbEntity create(DbEntity entity) throws DalException;
    Boolean update(Id id, DbEntity entity) throws DalException;
    Boolean delete(Id id) throws DalException;

    void createAsync(DbEntity entity);
    void updateAsync(Id id, DbEntity entity);
    void deleteAsync(Id id);
}
