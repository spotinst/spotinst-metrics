package com.spotinst.metrics.dal.models.elastic.common;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.*;

@Data
public class ElasticSearchRequest {
    private List<Filter> filters;
    private Integer      limit;

    public ElasticSearchRequest() {
        this.filters = new ArrayList<>();
    }

    @Data
    public static class Filter {
        private Map<String, Object>  matches;
        private List<Range>          ranges;

        public Filter(Map<String, Object> matches, List<Range> ranges) {
            this.matches = matches != null ? matches : new HashMap<>();
            this.ranges = ranges != null ? ranges : new ArrayList<>();
        }
    }

    @Data
    public static class Range {
        @NotNull
        private String name;
        @NotNull
        private Date   from;
        private Date   to;

        public Range(String name, Date from, Date to) {
            this.name = name;
            this.from = from;
            this.to = to;
        }
    }

    @Data
    public static class Aggregation {
        private Integer intervalInMinutes;
    }
}
