package com.spotinst.metrics.dal.services.elastic.infra;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import com.spotinst.metrics.MetricsAppContext;

public abstract class BaseElasticSearchService {
    protected ElasticsearchClient  elasticsearchClient;
    protected BulkIngester<Object> bulkIngester;

    public BaseElasticSearchService() {

        elasticsearchClient = MetricsAppContext.getInstance().getElasticClient();
        bulkIngester = MetricsAppContext.getInstance().getBulkIngester();
    }
}