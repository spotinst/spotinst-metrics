package com.spotinst.metrics.commons.configuration;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by aharontwizer on 7/18/15.
 */
@Setter
@Getter
public class ServicesConfig {

    @NotEmpty
    private String dbServiceUrl;

    @NotEmpty
    private String umsServiceUrl;

}
