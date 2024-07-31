package com.spotinst.service.commons.converters;

import com.spotinst.commons.converters.BaseConverter;
import com.spotinst.commons.mapper.entities.EntitiesMapper;
import com.spotinst.commons.enums.OrganizationRegistrationStateEnum;
import com.spotinst.service.commons.enums.INamedEnum;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

/**
 * Created by zachi.nachshon on 2/8/17.
 */
public class EnumConverter extends BaseConverter {

    private static final String ENTITY_TYPE = "Enum";

    public static final String CONVERTER_ORG_REG_STATE_ENUM_TO_INTEGER = "CONVERTER_ORG_REG_STATE_ENUM_TO_INTEGER";

    public EnumConverter() {
    }

    @Override
    protected String entityType() {
        return ENTITY_TYPE;
    }

    private static void stringToNamedEnumConverter() {
        EntitiesMapper.instance.registerCustomConverter(new BidirectionalConverter<String, INamedEnum>() {
            @Override
            public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {

                return sourceType.getRawType().equals(String.class) &&
                       INamedEnum.class.isAssignableFrom(destinationType.getRawType()) ||
                       destinationType.getRawType().equals(String.class) &&
                       INamedEnum.class.isAssignableFrom(sourceType.getRawType());
            }

            @Override
            public INamedEnum convertTo(String source, Type<INamedEnum> destinationType,
                                        MappingContext mappingContext) {
                INamedEnum byName = INamedEnum.searchEnum(destinationType.getRawType(), source);
                return byName;
            }

            @Override
            public String convertFrom(INamedEnum source, Type<String> destinationType, MappingContext mappingContext) {
                String retVal = source.getName();

                return retVal;
            }
        });
    }

    private static void orgRegistrationStateEnumToIntegerConverter() {
        EntitiesMapper.instance.registerCustomConverter(CONVERTER_ORG_REG_STATE_ENUM_TO_INTEGER,
                                                        new BidirectionalConverter<Integer, OrganizationRegistrationStateEnum>() {
                                                            @Override
                                                            public OrganizationRegistrationStateEnum convertTo(
                                                                    Integer source,
                                                                    Type<OrganizationRegistrationStateEnum> destinationType,
                                                                    MappingContext mappingContext) {

                                                                OrganizationRegistrationStateEnum byName =
                                                                        OrganizationRegistrationStateEnum
                                                                                .fromInteger(source);
                                                                return byName;
                                                            }

                                                            @Override
                                                            public Integer convertFrom(
                                                                    OrganizationRegistrationStateEnum source,
                                                                    Type<Integer> destinationType,
                                                                    MappingContext mappingContext) {

                                                                Integer retVal = source.getValue();
                                                                return retVal;
                                                            }
                                                        });
    }


    @Override
    protected void registerConvertersInner() {
        orgRegistrationStateEnumToIntegerConverter();
        stringToNamedEnumConverter();
    }

    @Override
    protected void registerMappingsInner() {
    }
}
