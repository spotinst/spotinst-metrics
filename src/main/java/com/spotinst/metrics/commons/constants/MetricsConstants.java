package com.spotinst.metrics.commons.constants;

public class MetricsConstants {

    public static class Resources {
        public static final String METRICS_RESOURCE_PATH = "/metric";
    }

    public static class FieldPath {
        public static final String METRIC_COUNT_FIELD_PATH_FORMAT = "metrics.%s.count";
        public static final String METRIC_VALUE_FIELD_PATH_FORMAT = "metrics.%s.value";
        public static final String METRIC_KEYWORD_SUFFIX          = ".keyword";
        public static final String ACCOUNT_ID_RAW_DIMENSION_NAME  = "accountId";
        public static final String DIMENSION_FIELD_PATH_FORMAT    = "dimensions.%s";
    }

    public static class Namespace {
        public static final String BALANCER_NAMESPACE_OLD = "balancer";
        public static final String BALANCER_NAMESPACE_NEW = "spotinst/balancer";

        public static final String SYSTEM_NAMESPACE_OLD = "system";
        public static final String SYSTEM_NAMESPACE_NEW = "spotinst/compute";
    }

    public static class ElasticMetricConstants {
        public static final String NAMESPACE_FIELD_PATH          = "namespace";
        public static final String TIMESTAMP_FIELD_PATH          = "timestamp";
        // Aggregation names
        public static final String AGG_GROUP_BY_DIMENSION_FORMAT = "GROUP_BY_DIMENSION_%s_AGG";
        public static final String DATE_HISTOGRAM_OFFSET_FORMAT  = "+%sm";

    }
}
