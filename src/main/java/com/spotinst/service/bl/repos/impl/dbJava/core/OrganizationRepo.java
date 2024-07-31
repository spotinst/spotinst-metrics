package com.spotinst.service.bl.repos.impl.dbJava.core;

import com.spotinst.service.bl.model.BlOrganization;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.ExceptionHelper;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.commons.models.dal.DbOrganization;
import com.spotinst.service.SampleAppContext;
import com.spotinst.service.bl.repos.interfaces.dbJava.core.IOrganizationRepo;
import com.spotinst.service.commons.configuration.SampleConfiguration;
import com.spotinst.service.commons.converters.Converters;
import com.spotinst.service.dal.models.core.organization.OrganizationFilter;
import com.spotinst.service.dal.models.core.organization.response.OrganizationResponse;
import com.spotinst.service.dal.services.db.infra.BulkRequest;
import com.spotinst.service.dal.services.db.infra.BulkSearchableGenericService;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zachi.nachshon on 7/30/17.
 */
public class OrganizationRepo implements IOrganizationRepo {

    //region Members
    private BulkSearchableGenericService<BigInteger, DbOrganization, OrganizationFilter, BulkRequest<DbOrganization>, OrganizationResponse>
            dbService;
    //endregion

    //region Constructors
    public OrganizationRepo() {
        SampleConfiguration configuration   = SampleAppContext.getInstance().getConfiguration();
        String              dbServiceUrl    = configuration.getServices().getDbServiceUrl();
        String              organizationUrl = "core/organization";
        String              url             = String.format("%s/%s", dbServiceUrl, organizationUrl);

        dbService = new BulkSearchableGenericService<>(url, true, OrganizationResponse.class);
    }
    //endregion

    //region Public Methods
    @Override
    public RepoGenericResponse<BlOrganization> get(BigInteger id) {
        RepoGenericResponse<BlOrganization> retVal;

        try {
            DbOrganization dbOrganization = dbService.get(id);
            BlOrganization blOrganization = Converters.Organization.toBl(dbOrganization);

            retVal = new RepoGenericResponse<>(blOrganization);
        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }

    @Override
    public RepoGenericResponse<List<BlOrganization>> getAll(OrganizationFilter filter) {
        RepoGenericResponse<List<BlOrganization>> retVal;

        try {
            List<DbOrganization> dbOrganizations = dbService.getAll(filter);
            List<BlOrganization> blOrganizations = new LinkedList<>();

            if (dbOrganizations != null) {
                for (DbOrganization dbOrganization : dbOrganizations) {
                    blOrganizations.add(Converters.Organization.toBl(dbOrganization));
                }

                retVal = new RepoGenericResponse<>(blOrganizations);
            }
            else {
                retVal = new RepoGenericResponse<>(false);
            }

        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }

    @Override
    public RepoGenericResponse<BlOrganization> create(BlOrganization organizations) {
        RepoGenericResponse<BlOrganization> retVal;

        try {
            DbOrganization dbOrganization    = Converters.Organization.toDal(organizations);
            DbOrganization newOrganization   = dbService.create(dbOrganization);
            BlOrganization newBlOrganization = Converters.Organization.toBl(newOrganization);

            retVal = new RepoGenericResponse<>(newBlOrganization);

        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }

    @Override
    public RepoGenericResponse<Boolean> createBulk(List<BlOrganization> organizations) {
        RepoGenericResponse<Boolean> retVal;

        try {
            List<DbOrganization> dbOrganizations = new LinkedList<>();

            for (BlOrganization blOrganization : organizations) {
                dbOrganizations.add(Converters.Organization.toDal(blOrganization));
            }

            Boolean              created         = dbService.createBulk(new BulkRequest<>(dbOrganizations));

            retVal = new RepoGenericResponse<>(created);

        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }

    @Override
    public RepoGenericResponse<Boolean> deleteBulk(List<BigInteger> ids) {
        RepoGenericResponse<Boolean> retVal;

        try {
            Boolean deleted = dbService.deleteBulk(ids);
            retVal = new RepoGenericResponse<>(deleted);

        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }

    @Override
    public RepoGenericResponse<Boolean> update(BigInteger id, BlOrganization organization) {
        RepoGenericResponse<Boolean> retVal;

        try {
            DbOrganization dbOrganization = Converters.Organization.toDal(organization);
            Boolean        updated        = dbService.update(id, dbOrganization);

            retVal = new RepoGenericResponse<>(updated);

        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }

    @Override
    public RepoGenericResponse<Boolean> updateBulk(List<BlOrganization> organizations) {
        RepoGenericResponse<Boolean> retVal;

        try {

            List<DbOrganization> dbOrganizations = new LinkedList<>();

            for (BlOrganization blOrganization : organizations) {
                dbOrganizations.add(Converters.Organization.toDal(blOrganization));
            }
            
            Boolean              updated         = dbService.updateBulk(new BulkRequest<>(dbOrganizations));

            retVal = new RepoGenericResponse<>(updated);

        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }
    //endregion


}
