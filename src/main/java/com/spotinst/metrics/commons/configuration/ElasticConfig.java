package com.spotinst.metrics.commons.configuration;

import lombok.Data;

/**
 * Created by Tal.Geva on 07/08/2024.
 */

@Data
public class ElasticConfig {
    //region Members
    private String  clusterScheme;
    private String  clusterHost;
    private Integer clusterPort;
    private Integer connectionTimeout;
    private Integer socketTimeout;
    private String  awsInterruptionRiskIndexName;
    private String  awsVersionInterruptionRiskIndexName;
    private String  awsInterruptionIndexName;
    private String  awsMarketUsageIndexName;
    private String  awsOrgMarketUsageIndexName;
    private String  awsMarketDailyAverageSpotPriceIndexName;
    private String  awsMarketSavingsIndexName;
    private String  awsAzZoneIdMappingIndexName;
    private String  awsMarketSpotPriceIndexName;
    private String  awsReplacementIndexName;
    private String  awsRIUtilStatisticsIndexName;
    private String  awsNfuRIUtilStatisticsIndexName;
    private String  awsSPUtilStatisticsIndexName;
    private String  awsRebalanceRecommendationIndexName;
    private String  azureInterruptionIndexName;
    private String  azureMarketUsageIndexName;
    private String  azureMarketEvictionRateIndexName;
    private String  gcpInterruptionIndexName;
    private String  azureMarketScoreIndexName;
    private String  azureMarketLaunchScoreResultIndexName;
    private String  azurePredictiveReplacementIndexName;
    private String  azureInDangerMarketIndexName;
    private String  azureTerminatedInstanceIndexName;
    private String  azureMissedInterruptionIndexName;
    private String  clusterRegion;
    //endregion
}
