package com.spotinst.service.commons.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zachi.nachshon on 2/8/17.
 */
public enum DummyEnum {
    FIRST("FIRST"),
    SECOND("SECOND"),
    THIRD("THIRD");

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyEnum.class);

    private String name;

    DummyEnum(String value) {
        this.name = value;
    }

    private static Map<String, DummyEnum> valuesMap;

    static {
        valuesMap = new HashMap<>();
        for(DummyEnum status : DummyEnum.values()) {
            valuesMap.put(status.name, status);
        }
    }

    public static DummyEnum fromName(String name) {
        DummyEnum retVal = null;

        if(valuesMap.containsKey(name)) {
            retVal = valuesMap.get(name);
        } else {
            LOGGER.error(String.format("Value [%s] is not part of the dummy type values supported by the DummyEnum", name));
        }

        return retVal;
    }

    public String getName() {
        return name;
    }
}
