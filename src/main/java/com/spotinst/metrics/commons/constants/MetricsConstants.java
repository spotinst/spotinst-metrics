package com.spotinst.metrics.commons.constants;

public class MetricsConstants {

    public static class Resources {
        public static final String METRICS_RESOURCE_PATH = "/metrics";
    }

    public static class FieldPath {
        public static final String METRIC_COUNT_FIELD_PATH_FORMAT     = "metrics.%s.count";
        public static final String METRIC_VALUE_FIELD_PATH_FORMAT     = "metrics.%s.value";
        public static final String METRIC_STATISTIC_FIELD_PATH_FORMAT = "metrics.%s.%s";
        public static final String METRIC_KEYWORD_SUFFIX              = ".keyword";
    }

    public static class Dimension {
        public static final String ACCOUNT_ID_RAW_DIMENSION_NAME = "accountId";
        public static final String DIMENSIONS_PATH_PREFIX             = "dimensions";
        public static final String METRIC_KEYWORD = ".keyword";
    }

    public static class Aggregations {
        public static final String AGG_METRIC_MIN_NAME     = "METRIC_MIN_AGG";
        public static final String AGG_METRIC_MAX_NAME     = "METRIC_MAX_AGG";
        public static final String AGG_METRIC_SUM_NAME     = "METRIC_SUM_AGG";
        public static final String AGG_METRIC_COUNT_NAME   = "METRIC_COUNT_AGG";
        public static final String AGG_METRIC_AVERAGE_NAME = "METRIC_AVERAGE_AGG";

        public static final String EMPTY_COUNT_BUCKET_FILTER_NAME = "EMPTY_COUNT_BUCKET_FILTER";

        public static final String AGG_PIPELINE_SUM_VALUE   = "sum_value";
        public static final String AGG_PIPELINE_COUNT_VALUE = "count_value";

    }

    public static class Namespace {
        public static final String BALANCER_NAMESPACE_OLD = "balancer";
        public static final String BALANCER_NAMESPACE_NEW = "spotinst/balancer";

        public static final String SYSTEM_NAMESPACE_OLD = "system";
        public static final String SYSTEM_NAMESPACE_NEW = "spotinst/compute";
    }
    public static class MetricScripts {
        public static final String SUM_AGG_SCRIPT_FORMAT_RAW_INDEX =
                "doc['metrics.%s.value'].value * doc['metrics.%s.count'].value;";
        public static final String AVG_BUCKET_SCRIPT_FORMAT         = "params.%s / params.%s";
        public static final String EMPTY_COUNT_BUCKET_SCRIPT_FORMAT = "params.%s > 0";


    }

    public static class ElasticMetricConstants {
        public static final String NAMESPACE_FIELD_PATH          = "namespace";
        public static final String TIMESTAMP_FIELD_PATH          = "timestamp";
        public static final String DIMENSION_FIELD_PATH_FORMAT   = "dimensions.%s";
        public static String OPTIMIZED_INDEX_PREFIX = "opt_";

        // Aggregation names
        public static final String AGG_GROUP_BY_DIMENSION_FORMAT = "GROUP_BY_DIMENSION_%s_AGG";
        public static final String AGG_RANGE_NAME                = "RANGE_AGG";
        public static final String AGG_DATE_HISTOGRAM_NAME       = "DATE_HISTOGRAM_AGG";

        public static final String DATE_HISTOGRAM_OFFSET_FORMAT = "+%sm";

    }
}
