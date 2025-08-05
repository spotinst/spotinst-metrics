package com.spotinst.metrics.commons.enums;

/**
 * @author Oran Shuster
 * @since 28/10/2019
 */
public interface INamedEnum {
    String getName();

    static <T extends INamedEnum> T searchEnum(Class<T> enumeration, String search) {
        for (T each : enumeration.getEnumConstants()) {
            if (each.getName().compareToIgnoreCase(search) == 0) {
                return each;
            }
        }
        return null;
    }
}
