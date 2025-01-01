package com.spotinst.metrics.commons.configuration;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Created by Tal.Geva on 07/08/2024.
 */

@Data
public class ElasticConfig {
    //region Members
    private String  scheme;
    private Integer connectionTimeout;
    private Integer socketTimeout;
    private String  clusterRegion;
    private IndexNamePatterns indexNamePatterns;
    private Set<String> indexNameOverriddenPatterns;
    private String username;
    private String password;

    @NotNull
    private String host;

    @NotNull
    @Max(65535)
    @Min(1024)
    private Integer port;

    @NotNull
    private String clusterName;

    private Boolean           transportSniff;
    private Integer           pingTimeout;

    @Valid
    @NotNull
    private BulkProcessorIndexerConfig bulkProcessorIndexer;

    public ElasticConfig() {
    }

    //endregion
}
