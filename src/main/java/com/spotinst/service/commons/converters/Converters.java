package com.spotinst.service.commons.converters;

import com.spotinst.commons.converters.organization.OrganizationConverter;
import com.spotinst.commons.converters.suppliers.BlOrganizationSupplier;
import com.spotinst.commons.converters.suppliers.DalOrganizationSupplier;
import com.spotinst.commons.models.dal.DbOrganization;
import com.spotinst.service.bl.model.BlOrganization;
import com.spotinst.service.commons.converters.dummy.DummyConverter;
import com.spotinst.service.commons.converters.notification.NotificationConverter;

/**
 * Created by zachi.nachshon on 4/2/17.
 */
public class Converters {
    public static EnumConverter         Enum         = new EnumConverter();
    public static MapConverter          Map          = new MapConverter();
    public static SetConverter          Set          = new SetConverter();
    public static ListConverter         List         = new ListConverter();
    public static CommonConverter       Common       = new CommonConverter();
    public static DummyConverter        Dummy        = new DummyConverter();
    public static NotificationConverter Notification = new NotificationConverter();
    // organization
    private static BlOrganizationSupplier<BlOrganization>  blSupplier  = new BlOrganizationSupplier<>(BlOrganization::new);
    private static DalOrganizationSupplier<DbOrganization> dalSupplier = new DalOrganizationSupplier<>(DbOrganization::new);
    public static OrganizationConverter<BlOrganization, DbOrganization> Organization = new OrganizationConverter<>(blSupplier, dalSupplier);
}
