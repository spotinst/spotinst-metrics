package com.spotinst.service.commons.converters;

import com.spotinst.commons.converters.BaseConverter;
import com.spotinst.commons.mapper.entities.EntitiesMapper;
import com.spotinst.commons.mapper.json.JsonMapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oran Shuster
 * @since 19/10/2019
 */
public class MapConverter extends BaseConverter {

    private void mapToJsonConverter() {
        EntitiesMapper.instance.registerCustomConverter(new BidirectionalConverter<Map<Object, Object>, String>() {

            @Override
            public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
                return sourceType.getRawType().equals(String.class) &&
                       Map.class.isAssignableFrom(destinationType.getRawType()) ||
                       destinationType.getRawType().equals(String.class) &&
                       Map.class.isAssignableFrom(sourceType.getRawType());
            }

            @Override
            public String convertTo(Map<Object, Object> source, Type<String> destinationType,
                                    MappingContext mappingContext) {
                String retVal = JsonMapper.toJson(source);
                return retVal;
            }

            @Override
            public Map<Object, Object> convertFrom(String source, Type<Map<Object, Object>> destinationType,
                                                   MappingContext mappingContext) {
                Map<Object, Object> retVal = JsonMapper.fromJson(source, destinationType.getRawType());

                return retVal;
            }
        });
    }

    private void mapGenericConverter() {
        EntitiesMapper.instance
                .registerCustomConverter(new BidirectionalConverter<Map<Object, Object>, Map<Object, Object>>() {

                    @Override
                    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
                        return sourceType.getRawType().equals(Map.class) &&
                               Map.class.isAssignableFrom(destinationType.getRawType()) ||
                               destinationType.getRawType().equals(Map.class) &&
                               Map.class.isAssignableFrom(sourceType.getRawType());
                    }

                    @Override
                    public Map<Object, Object> convertTo(Map<Object, Object> source,
                                                         Type<Map<Object, Object>> destinationType,
                                                         MappingContext mappingContext) {
                        Map<Object, Object> retVal = new HashMap<>();

                        for (Map.Entry<Object, Object> entry : source.entrySet()) {
                            Object key   = entry.getKey();
                            Object value = entry.getValue();
                            retVal.put(key, value);
                        }

                        return retVal;
                    }

                    @Override
                    public Map<Object, Object> convertFrom(Map<Object, Object> source,
                                                           Type<Map<Object, Object>> destinationType,
                                                           MappingContext mappingContext) {
                        Map<Object, Object> retVal = new HashMap<>();

                        for (Map.Entry<Object, Object> entry : source.entrySet()) {
                            Object key   = entry.getKey();
                            Object value = entry.getValue();
                            retVal.put(key, value);
                        }

                        return retVal;
                    }
                });
    }

    @Override
    protected void registerConvertersInner() {
        mapGenericConverter();
        mapToJsonConverter();
    }

    @Override
    protected void registerMappingsInner() {

    }

    @Override
    protected String entityType() {
        return "Map";
    }
}
