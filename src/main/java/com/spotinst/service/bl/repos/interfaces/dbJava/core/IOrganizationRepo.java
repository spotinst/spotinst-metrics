package com.spotinst.service.bl.repos.interfaces.dbJava.core;

import com.spotinst.service.bl.model.BlOrganization;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.service.dal.models.core.organization.OrganizationFilter;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by zachi.nachshon on 7/30/17.
 */
public interface IOrganizationRepo {
    RepoGenericResponse<BlOrganization> get(BigInteger id);

    RepoGenericResponse<List<BlOrganization>> getAll(OrganizationFilter filter);

    RepoGenericResponse<BlOrganization> create(BlOrganization organization);

    RepoGenericResponse<Boolean> createBulk(List<BlOrganization> organizations);

    RepoGenericResponse<Boolean> deleteBulk(List<BigInteger> id);

    RepoGenericResponse<Boolean> update(BigInteger id, BlOrganization organization);

    RepoGenericResponse<Boolean> updateBulk(List<BlOrganization> organizations);


}
