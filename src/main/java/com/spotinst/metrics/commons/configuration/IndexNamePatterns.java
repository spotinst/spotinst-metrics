package com.spotinst.metrics.commons.configuration;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IndexNamePatterns {
    private String readPattern;
    private String writePattern;
    private String aggregationReadPattern;

    public IndexNamePatterns() {
    }

}

