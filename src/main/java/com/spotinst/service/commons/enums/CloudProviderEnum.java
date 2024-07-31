package com.spotinst.service.commons.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CloudProviderEnum implements INamedEnum {
    AWS("aws"),
    GCP("gcp"),
    AZURE("azure");

    private String name;

    CloudProviderEnum(String name) {
        this.name = name;
    }

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CloudProviderEnum.class);

    @JsonCreator
    public static CloudProviderEnum fromName(String name) {
        CloudProviderEnum retVal = null;

        for (CloudProviderEnum cloudProviderEnum : CloudProviderEnum.values()) {
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(name, cloudProviderEnum.name)) {
                retVal = cloudProviderEnum;
                break;
            }
        }

        if (retVal == null) {
            LOGGER.warn(
                    String.format("Tried to create cloud provider Enum for: %s, but we don't support such type", name));
        }

        return retVal;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}