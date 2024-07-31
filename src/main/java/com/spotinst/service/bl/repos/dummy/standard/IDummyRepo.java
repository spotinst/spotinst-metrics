package com.spotinst.service.bl.repos.dummy.standard;

import com.spotinst.dropwizard.common.repo.IRepository;
import com.spotinst.service.bl.model.dummy.BlDummy;
import com.spotinst.service.dal.models.db.dummy.DummyFilter;

/**
 * This is the full repository implementation option, usually gets duplicated for every entity.
 *
 * Created by zachi.nachshon on 2/8/17.
 */
public interface IDummyRepo extends IRepository<BlDummy, DummyFilter, String> {
}
