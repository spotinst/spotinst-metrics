package com.spotinst.service.commons.converters.dummy;

import com.spotinst.commons.converters.BaseConverter;
import com.spotinst.commons.mapper.entities.EntitiesMapper;
import com.spotinst.commons.mapper.entities.MappingPair;
import com.spotinst.dropwizard.bl.repo.IGenericRepoConverter;
import com.spotinst.service.api.models.dummy.ApiDummyConfig;
import com.spotinst.service.api.models.dummy.ApiDummyCreateInner;
import com.spotinst.service.api.models.dummy.ApiDummyResponse;
import com.spotinst.service.api.models.dummy.ApiDummyUpdateInner;
import com.spotinst.service.bl.model.dummy.BlDummy;
import com.spotinst.service.bl.model.dummy.BlDummyConfig;
import com.spotinst.service.dal.models.db.dummy.DbDummy;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zachi.nachshon on 2/8/17.
 */
public class DummyConverter extends BaseConverter implements IGenericRepoConverter<BlDummy, DbDummy> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyConverter.class);

    private static final String ENTITY_TYPE = "Dummy";

    private static final String CONVERTER_BL_DUMMY_CONFIG_LIST_TO_MAP = "CONVERTER_BL_DUMMY_CONFIG_LIST_TO_MAP";

    public DummyConverter() {
    }

    @Override
    protected String entityType() {
        return ENTITY_TYPE;
    }

    // region toApi
    public ApiDummyResponse toApi(BlDummy entity) {
        ApiDummyResponse retVal = EntitiesMapper.instance.mapType(entity, ApiDummyResponse.class);
        return retVal;
    }
    // endregion

    // region toBl
    public BlDummy toBl(ApiDummyCreateInner entity) {
        BlDummy retVal = EntitiesMapper.instance.mapType(entity, BlDummy.class);
        return retVal;
    }

    public BlDummy toBl(ApiDummyUpdateInner entity) {
        BlDummy retVal = EntitiesMapper.instance.mapType(entity, BlDummy.class);
        return retVal;
    }

    public BlDummy toBl(DbDummy entity) {
        BlDummy retVal = EntitiesMapper.instance.mapType(entity, BlDummy.class);
        return retVal;
    }

    @Override
    public List<BlDummy> toBl(List<DbDummy> dbEntities) {
        List<BlDummy> retVal = EntitiesMapper.instance.mapAsList(dbEntities, BlDummy.class);
        return retVal;
    }
    // endregion

    // region toDb
    public DbDummy toDb(BlDummy entity) {
        DbDummy retVal = EntitiesMapper.instance.mapType(entity, DbDummy.class);
        return retVal;
    }

    @Override
    public List<DbDummy> toDb(List<BlDummy> blEntities) {
        List<DbDummy> retVal = EntitiesMapper.instance.mapAsList(blEntities, DbDummy.class);
        return retVal;
    }
    // endregion

    // region Register Converters
    @Override
    protected void registerConvertersInner() {

        // In here we should register properties that differ in their types and how to map them
        // Example: property named 'config' which is of type List<BlDummyConfig> on BlXXX and Map<String, String> String on DbXXX
        blDummyConfigListToMap();
    }

    private void blDummyConfigListToMap() {
        EntitiesMapper.instance.registerCustomConverter(CONVERTER_BL_DUMMY_CONFIG_LIST_TO_MAP,
                                                        new BidirectionalConverter<List<BlDummyConfig>, Map<String, String>>() {
                                                            @Override
                                                            public Map<String, String> convertTo(
                                                                    List<BlDummyConfig> source,
                                                                    Type<Map<String, String>> destinationType,
                                                                    MappingContext mappingContext) {
                                                                Map<String, String> retVal = null;

                                                                if (source != null) {
                                                                    retVal = new HashMap<>();
                                                                    for (BlDummyConfig config : source) {
                                                                        retVal.put(config.getKey(), config.getValue());
                                                                    }
                                                                }

                                                                return retVal;
                                                            }

                                                            @Override
                                                            public List<BlDummyConfig> convertFrom(
                                                                    Map<String, String> source,
                                                                    Type<List<BlDummyConfig>> destinationType,
                                                                    MappingContext mappingContext) {
                                                                List<BlDummyConfig> retVal = null;

                                                                if (source != null) {
                                                                    retVal = new ArrayList<>();
                                                                    for (String key : source.keySet()) {
                                                                        String value = source.get(key);

                                                                        BlDummyConfig newConf = new BlDummyConfig();
                                                                        newConf.setKey(key);
                                                                        newConf.setValue(value);

                                                                        retVal.add(newConf);
                                                                    }
                                                                }

                                                                return retVal;
                                                            }
                                                        });
    }
    // endregion

    // region Register Mappings
    @Override
    protected void registerMappingsInner() {

        // In here we should register class mappings that built on top of pre-defined converters
        // Example: ApiXXX should be mapped to BlXXX with special converter for 'config' property

        List<MappingPair<String, String>> apiDummyToBlDummy = new ArrayList<>();
        // Mapping Scenario: map different prop names with same type
        //
        //  From: endpoints     (ApiDummyCreateInner)
        //  To:   endpointData  (BlDummy, DbDummy)
        //
        apiDummyToBlDummy.add(new MappingPair<>("endpoints", "endpointData"));

        // Mapping Scenario: map component structure into a flat structure
        //
        //  From: ApiDummyConnectionInfo.protocol & ApiDummyConnectionInfo.port  (ApiDummyCreateInner)
        //  To:   protocol & port                                                (BlDummy, DbDummy)
        //
        apiDummyToBlDummy.add(new MappingPair<>("connectionInfo.protocol", "protocol"));
        apiDummyToBlDummy.add(new MappingPair<>("connectionInfo.port", "port"));

        EntitiesMapper.instance
                .register(ApiDummyCreateInner.class, BlDummy.class, apiDummyToBlDummy, MAP_NULLS_ENABLED);

        EntitiesMapper.instance
                .register(ApiDummyUpdateInner.class, BlDummy.class, apiDummyToBlDummy, MAP_NULLS_ENABLED);

        EntitiesMapper.instance.register(ApiDummyResponse.class, BlDummy.class, apiDummyToBlDummy, MAP_NULLS_ENABLED);

        List<MappingPair<String, String>> blDummyToDbDummy = new ArrayList<>();
        //        blDummyToDbDummy.add(new MappingPair<>("config", "config", CONVERTER_BL_DUMMY_CONFIG_LIST_TO_MAP));

        EntitiesMapper.instance.register(BlDummy.class, DbDummy.class, blDummyToDbDummy, MAP_NULLS_ENABLED);

        EntitiesMapper.instance.register(ApiDummyConfig.class, BlDummyConfig.class);

    }
    // endregion
}
