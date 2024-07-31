package com.spotinst.service.commons.enums;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aharontwizer on 8/30/15.
 */
public enum EventIssuerEnum implements INamedEnum {

    USER("user"),
    OPTIMIZER("optimizer"),
    MONITOR("monitor"),
    UNDEFINED("undefined");

    //region Members
    private final        String name;
    private static final Logger LOGGER = LoggerFactory.getLogger(EventIssuerEnum.class);
    //endregion

    //region Constructors
    EventIssuerEnum(String name) {
        this.name = name;
    }
    //endregion

    //region Getters and Setters
    public String getName() {
        return name;
    }
    //endregion

    //region Public Methods
    public static EventIssuerEnum fromName(String name) {
        EventIssuerEnum retVal = null;

        for (EventIssuerEnum instanceMarketEnum : EventIssuerEnum.values()) {
            if (StringUtils.equalsIgnoreCase(name, instanceMarketEnum.name)) {
                retVal = instanceMarketEnum;
                break;
            }
        }

        if (retVal == null) {
            LOGGER.warn(
                    "Tried to create group event issuer type enum for: " + name + ", but we don't support such type ");
        }
        return retVal;
    }
    //endregion
}
