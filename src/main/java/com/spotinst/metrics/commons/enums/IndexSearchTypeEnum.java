package com.spotinst.metrics.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public enum IndexSearchTypeEnum implements INamedEnum {
    FULL_TEXT("fullText"),
    EXACT_MATCH("exactMatch");

    //region Members
    private final        String name;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    //endregion

    //region Constructors
    IndexSearchTypeEnum(String name) {
        this.name = name;
    }
    //endregion

    //region Getters and Setters]
    @JsonValue
    public String getName() {
        return name;
    }
    //endregion

    //region Public Methods
    public static IndexSearchTypeEnum fromName(String name) {
        IndexSearchTypeEnum retVal = null;

        for (IndexSearchTypeEnum indexSearchTypeEnum : IndexSearchTypeEnum.values()) {
            if (StringUtils.equalsIgnoreCase(name, indexSearchTypeEnum.name)) {
                retVal = indexSearchTypeEnum;
                break;
            }
        }

        if (retVal == null) {
            LOGGER.error(String.format("Tried to create %s for: %s, but we don't support such type ",
                                       MethodHandles.lookup().lookupClass().getSimpleName(), name));
        }
        return retVal;
    }
    //endregion
}
