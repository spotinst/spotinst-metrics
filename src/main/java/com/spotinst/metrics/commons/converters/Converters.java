package com.spotinst.metrics.commons.converters;

import com.spotinst.commons.converters.organization.OrganizationConverter;
import com.spotinst.commons.converters.suppliers.BlOrganizationSupplier;
import com.spotinst.commons.converters.suppliers.DalOrganizationSupplier;
import com.spotinst.commons.models.dal.DbOrganization;
import com.spotinst.metrics.bl.model.BlOrganization;

/**
 * Created by zachi.nachshon on 4/2/17.
 */
public class Converters {
    public static EnumConverter         Enum         = new EnumConverter();
    public static CommonConverter       Common       = new CommonConverter();
    // organization
    private static BlOrganizationSupplier<BlOrganization>  blSupplier  = new BlOrganizationSupplier<>(BlOrganization::new);
    private static DalOrganizationSupplier<DbOrganization> dalSupplier = new DalOrganizationSupplier<>(DbOrganization::new);
    public static OrganizationConverter<BlOrganization, DbOrganization> Organization = new OrganizationConverter<>(blSupplier, dalSupplier);
}
