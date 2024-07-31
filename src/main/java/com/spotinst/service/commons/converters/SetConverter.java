package com.spotinst.service.commons.converters;

import com.spotinst.commons.converters.BaseConverter;
import com.spotinst.commons.mapper.entities.EntitiesMapper;
import com.spotinst.commons.mapper.json.JsonMapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Oran Shuster
 * @since 19/10/2019
 */
public class SetConverter extends BaseConverter {

    private void setToJsonConverter() {
        EntitiesMapper.instance.registerCustomConverter(new BidirectionalConverter<Set<Object>, String>() {

            @Override
            public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
                return sourceType.getRawType().equals(String.class) &&
                       Set.class.isAssignableFrom(destinationType.getRawType()) ||
                       destinationType.getRawType().equals(String.class) &&
                       Set.class.isAssignableFrom(sourceType.getRawType());
            }

            @Override
            public String convertTo(Set<Object> source, Type<String> destinationType, MappingContext mappingContext) {
                String retVal = JsonMapper.toJson(source);
                return retVal;
            }

            @Override
            public Set<Object> convertFrom(String source, Type<Set<Object>> destinationType,
                                           MappingContext mappingContext) {
                Set<Object> retVal = JsonMapper.fromJson(source, destinationType.getRawType());

                return retVal;
            }
        });
    }

    private void setGenericConverter() {
        EntitiesMapper.instance.registerCustomConverter(new BidirectionalConverter<Set<Object>, Set<Object>>() {

            @Override
            public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
                return sourceType.getRawType().equals(Set.class) &&
                       Set.class.isAssignableFrom(destinationType.getRawType()) ||
                       destinationType.getRawType().equals(Set.class) &&
                       Set.class.isAssignableFrom(sourceType.getRawType());
            }

            @Override
            public Set<Object> convertTo(Set<Object> source, Type<Set<Object>> destinationType,
                                         MappingContext mappingContext) {
                Set<Object> retVal = new HashSet<>(source);
                return retVal;
            }

            @Override
            public Set<Object> convertFrom(Set<Object> source, Type<Set<Object>> destinationType,
                                           MappingContext mappingContext) {
                Set<Object> retVal = new HashSet<>(source);
                return retVal;
            }
        });
    }

    @Override
    protected void registerConvertersInner() {
        setGenericConverter();
        setToJsonConverter();
    }

    @Override
    protected void registerMappingsInner() {

    }

    @Override
    protected String entityType() {
        return "Set";
    }
}
