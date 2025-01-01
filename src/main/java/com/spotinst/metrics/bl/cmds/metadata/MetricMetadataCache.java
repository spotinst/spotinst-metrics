//package com.spotinst.metrics.bl.cmds.metadata;
//
//import com.spotinst.metrics.MetricsAppContext;
//import com.spotinst.metrics.bl.model.BlMetric;
//import com.spotinst.metrics.bl.model.metadata.BlMetricMetadata;
//import com.spotinst.metrics.bl.model.metadata.BlNamespaceDimensionPair;
//import com.spotinst.metrics.commons.configuration.MetadataCacheConfig;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class MetricMetadataCache {
//    private static final Logger LOGGER = LoggerFactory.getLogger(MetricMetadataCache.class);
//
//    private static final Integer MAX_CACHED_DIMENSIONS_LIMIT_PER_ACCOUNT;
//    private static final Integer MAX_CACHED_METRICS_LIMIT_PER_ACCOUNT_DIMENSION;
//
//    public static final MetricMetadataCache instance = new MetricMetadataCache();
//
//    private final Object lockObj = new Object();
//
//    private Map<String, Map<BlNamespaceDimensionPair, Set<BlMetric>>> cacheMap;
//
//    private MetricMetadataCache() {
//        this.cacheMap = new HashMap<>();
//    }
//
//    static {
//        MetadataCacheConfig metricsConfig = MetricsAppContext.getInstance().getConfiguration().getMetadataCache();
//        MAX_CACHED_DIMENSIONS_LIMIT_PER_ACCOUNT = metricsConfig.getMaxCachedDimensionsLimitPerAccount();
//        MAX_CACHED_METRICS_LIMIT_PER_ACCOUNT_DIMENSION = metricsConfig.getMaxCachedMetricsLimitPerAccountDimension();
//
//        String format = "Initializing MetricMetadataCache with max cache limit per account of [%s] dimensions and [%s] metrics per dimension";
//        LOGGER.info(String.format(format,
//                                  MAX_CACHED_DIMENSIONS_LIMIT_PER_ACCOUNT,
//                                  MAX_CACHED_METRICS_LIMIT_PER_ACCOUNT_DIMENSION));
//    }
//
//    public void put(String accountId, BlMetricMetadata blMetricMetadata) {
//        Map<BlNamespaceDimensionPair, Set<BlMetric>> metadataMap;
//        metadataMap = cacheMap.containsKey(accountId) ? cacheMap.get(accountId) : new HashMap<>();
//
//        int dimSizePerAccount = metadataMap.size();
//        if(dimSizePerAccount <= MAX_CACHED_DIMENSIONS_LIMIT_PER_ACCOUNT) {
//            BlNamespaceDimensionPair pair = blMetricMetadata.getNamespaceDimensionPair();
//            if(pair != null) {
//                Set<BlMetric> metrics;
//                metrics = metadataMap.containsKey(pair) ? metadataMap.get(pair) : new HashSet<>();
//
//                List<BlMetric> metricsToAppend = blMetricMetadata.getMetrics();
//                if (metricsToAppend != null && metricsToAppend.isEmpty() == false) {
//                    // Remove the value content to avoid wrong equals which result in
//                    // saving the same metric name/unit to the result set
//                    Set<BlMetric> metricsWithoutValue = metricsToAppend.stream()
//                                                                       .map(m -> new BlMetric(m.getName(), m.getUnit()))
//                                                                       .collect(Collectors.toSet());
//                    metrics.addAll(metricsWithoutValue);
//
//                    int newCachedMetricSize = metrics.size();
//                    if(newCachedMetricSize <= MAX_CACHED_METRICS_LIMIT_PER_ACCOUNT_DIMENSION) {
//                        metadataMap.put(pair, metrics);
//                        updateCache(accountId, metadataMap);
//                    } else {
//                        String format = "Metrics per dimension quota reached for account [%s], size exceeds max limit of [%s], skipping cache";
//                        String errMsg = String.format(format, accountId, MAX_CACHED_METRICS_LIMIT_PER_ACCOUNT_DIMENSION);
//                        // Can produce a large amount of logs - lower log level if needed
//                        LOGGER.debug(errMsg);
//                    }
//                }
//            }
//        } else {
//            String format = "Dimensions quota reached for account [%s], size exceeds max limit of [%s], skipping cache";
//            String errMsg = String.format(format, accountId, MAX_CACHED_DIMENSIONS_LIMIT_PER_ACCOUNT);
//            // Can produce a large amount of logs - lower log level if needed
//            LOGGER.debug(errMsg);
//        }
//    }
//
//    public Map<String, Map<BlNamespaceDimensionPair, Set<BlMetric>>> getMetadataAndInvalidateCache() {
//        Map<String, Map<BlNamespaceDimensionPair, Set<BlMetric>>> toFlush;
//
//        // Keep previous memory pointer
//        toFlush = this.cacheMap;
//
//        synchronized(lockObj) {
//            this.cacheMap = new HashMap<>();
//        }
//
//        return toFlush;
//    }
//
//    private void updateCache(String accountId,
//                             Map<BlNamespaceDimensionPair, Set<BlMetric>> metadataMap) {
//
//        synchronized(lockObj) {
//            cacheMap.put(accountId, metadataMap);
//        }
//    }
//}