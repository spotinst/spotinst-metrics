package com.spotinst.metrics.dal.services.db.infra;

import com.spotinst.dropwizard.bl.repo.IEntityFilter;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;

import java.util.List;

/**
 * Created by zachi.nachshon on 7/15/17.
 */
public interface IBulkSearchableGenericService<Id, DbEntity, Filter extends IEntityFilter, BulkRequest, SearchRequest> extends IBulkGenericService<Id, DbEntity, Filter, BulkRequest> {
    List<DbEntity> search(SearchRequest request) throws DalException;
}
