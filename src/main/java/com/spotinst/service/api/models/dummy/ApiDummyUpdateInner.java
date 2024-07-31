package com.spotinst.service.api.models.dummy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spotinst.commons.mapper.entities.base.PartialUpdateEntity;
import io.dropwizard.validation.OneOf;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Created by zachi.nachshon on 4/6/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDummyUpdateInner extends PartialUpdateEntity {

    // Mapping Scenario: no need to supply special converter for this property
    //
    //  From: String    (ApiDummyCreateInner, BlDummy, DbDummy)
    //  To:   String    (ApiDummyCreateInner, BlDummy, DbDummy)
    //
    private String name;

    // Mapping Scenario: map same prop name between different types
    //
    //  From: String    (ApiDummyCreateInner, DbDummy)
    //  To:   Enum      (BlDummy)
    //
    @Nullable
    @OneOf(value={"FIRST", "SECOND", "THIRD"})
    private String type;

    // Mapping Scenario: map same prop name between different types
    //
    //  From: List<ApiDummyConfig>  (ApiDummyCreateInner, BlDummy)
    //  To:   Map<String, String>   (DbDummy)
    //
//    @Valid
//    @Nullable
//    @Size(min=1)
//    private List<ApiDummyConfig> config;

    // Mapping Scenario: map different prop names with same type
    //
    //  From: endpoints     (ApiDummyCreateInner)
    //  To:   endpointData  (BlDummy, DbDummy)
    //
    @Nullable
    @Size(max=100)
    private List<Integer> endpoints;

    // Mapping Scenario: map component structure into a flat structure
    //
    //  From: ApiDummyConnectionInfo.protocol & ApiDummyConnectionInfo.port  (ApiDummyCreateInner)
    //  To:   protocol & port                                                (BlDummy, DbDummy)
    //
    @Valid
    @Nullable
    private ApiDummyConnectionInfo connectionInfo;

    public ApiDummyUpdateInner() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        touch("name");
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        touch("type");
        this.type = type;
    }

//    public List<ApiDummyConfig> getConfig() {
//        return config;
//    }
//
//    public void setConfig(List<ApiDummyConfig> config) {
//        touch("config");
//        this.config = config;
//    }

    public List<Integer> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Integer> endpoints) {
        touch("endpoints");
        this.endpoints = endpoints;
    }

    public ApiDummyConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(ApiDummyConnectionInfo connectionInfo) {
        touch("connectionInfo");
        this.connectionInfo = connectionInfo;
    }
}
