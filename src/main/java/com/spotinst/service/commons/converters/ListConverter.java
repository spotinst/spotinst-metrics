package com.spotinst.service.commons.converters;

import com.spotinst.commons.converters.BaseConverter;
import com.spotinst.commons.mapper.entities.EntitiesMapper;
import com.spotinst.commons.mapper.json.JsonMapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oran Shuster
 * @since 19/10/2019
 */
public class ListConverter extends BaseConverter {

    private void listToJsonConverter() {
        EntitiesMapper.instance.registerCustomConverter(new BidirectionalConverter<List<Object>, String>() {

            @Override
            public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
                return sourceType.getRawType().equals(String.class) &&
                       List.class.isAssignableFrom(destinationType.getRawType()) ||
                       destinationType.getRawType().equals(String.class) &&
                       List.class.isAssignableFrom(sourceType.getRawType());
            }

            @Override
            public String convertTo(List<Object> source, Type<String> destinationType, MappingContext mappingContext) {
                String retVal = JsonMapper.toJson(source);
                return retVal;
            }

            @Override
            public List<Object> convertFrom(String source, Type<List<Object>> destinationType,
                                            MappingContext mappingContext) {
                List<Object> retVal = JsonMapper.fromJson(source, destinationType.getRawType());

                return retVal;
            }
        });
    }

    private void listGenericConverter() {
        EntitiesMapper.instance.registerCustomConverter(new BidirectionalConverter<List<Object>, List<Object>>() {

            @Override
            public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
                return sourceType.getRawType().equals(List.class) &&
                       List.class.isAssignableFrom(destinationType.getRawType()) ||
                       destinationType.getRawType().equals(List.class) &&
                       List.class.isAssignableFrom(sourceType.getRawType());
            }

            @Override
            public List<Object> convertTo(List<Object> source, Type<List<Object>> destinationType,
                                          MappingContext mappingContext) {
                List<Object> retVal = new ArrayList<>(source);
                return retVal;
            }

            @Override
            public List<Object> convertFrom(List<Object> source, Type<List<Object>> destinationType,
                                            MappingContext mappingContext) {
                List<Object> retVal = new ArrayList<>(source);
                return retVal;
            }
        });
    }

    @Override
    protected void registerConvertersInner() {
        listGenericConverter();
        listToJsonConverter();
    }

    @Override
    protected void registerMappingsInner() {

    }

    @Override
    protected String entityType() {
        return "List";
    }
}
