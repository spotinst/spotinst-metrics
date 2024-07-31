package com.spotinst.service.dal.services.db.infra;

import com.spotinst.dropwizard.bl.repo.IEntityFilter;
import com.spotinst.dropwizard.bl.service.IGenericService;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;

import java.util.List;

/**
 * Created by zachi.nachshon on 7/15/17.
 */
public interface ISearchableGenericService<Id, DbEntity, Filter extends IEntityFilter, SearchRequest>
        extends IGenericService<Id, DbEntity, Filter> {
    List<DbEntity> search(SearchRequest request) throws DalException;
}
