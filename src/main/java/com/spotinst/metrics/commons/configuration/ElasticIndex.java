package com.spotinst.metrics.commons.configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElasticIndex {
    //region Members
    private String name;
    private String templateFileName;
    private String searchType;
    //endregion

}
