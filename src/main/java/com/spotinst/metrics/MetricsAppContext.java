//package com.spotinst.metrics;
//
//import com.spotinst.dropwizard.common.context.BaseAppContext;
//import com.spotinst.metrics.bl.model.BlOrganization;
//import com.spotinst.metrics.commons.configuration.MetricsConfiguration;
//import com.spotinst.metrics.dal.services.elastic.ElasticSearchPercentilesService;
//import com.spotinst.metrics.dal.services.elastic.IElasticSearchPercentilesService;
//import com.spotinst.metrics.dal.services.elastic.infra.ElasticSearchService;
//import com.spotinst.metrics.dal.services.elastic.infra.IElasticSearchService;
////import org.elasticsearch.client.RestHighLevelClient;
//import co.elastic.clients.transport.rest_client.RestClientTransport;
//
//import java.math.BigInteger;
//import java.util.Map;
//
//public class MetricsAppContext extends BaseAppContext<MetricsConfiguration, BlOrganization> {
//
//    private static MetricsAppContext instance;
//
//    private MetricsAppContext() {
//    }
//
//    public static MetricsAppContext getInstance() {
//
//        if (instance == null) {
//            synchronized (MetricsAppContext.class) {
//                if (instance == null) {
//                    instance = new MetricsAppContext();
//                }
//            }
//        }
//        return instance;
//    }
//
//    //region Members
//    private Map<BigInteger, BlOrganization> organizations;
//    private RestHighLevelClient             elasticClient;
//    private IElasticSearchService            elasticSearchService;
//    private IElasticSearchPercentilesService elasticSearchPercentilesService;
//
//    //endregion
//
//    // Method to get the ElasticSearchService
//    public IElasticSearchService getElasticSearchService() {
//        if (elasticSearchService == null) {
//            // Initialize the elasticSearchService here
//            elasticSearchService = new ElasticSearchService(); // Ensure this is a valid instance
//        }
//        return elasticSearchService;
//    }
//
//    // Method to get the ElasticSearchService
//    public IElasticSearchPercentilesService getElasticSearchPercentilesService() {
//        if (elasticSearchPercentilesService == null) {
//            // Initialize the elasticSearchService here
//            elasticSearchPercentilesService = new ElasticSearchPercentilesService(); // Ensure this is a valid instance
//        }
//        return elasticSearchPercentilesService;
//    }
//
//    //region Getters and Setters
//
//    @Override
//    public Map<BigInteger, BlOrganization> getOrganizations() {
//        return organizations;
//    }
//
//    @Override
//    public void setOrganizations(Map<BigInteger, BlOrganization> organizations) {
//        this.organizations = organizations;
//    }
//
//    public RestHighLevelClient getElasticClient() {
//        return elasticClient;
//    }
//
//    public void setElasticClient(RestHighLevelClient elasticClient) {
//        this.elasticClient = elasticClient;
//    }
//
////    public IElasticSearchService getElasticSearchService() {
////        return elasticSearchService;
////    }
//
//    public void setElasticSearchService(IElasticSearchService elasticSearchService) {
//        this.elasticSearchService = elasticSearchService;
//    }
//
//    public void setElasticSearchPercentilesService(IElasticSearchPercentilesService elasticSearchPercentilesService) {
//        this.elasticSearchPercentilesService = elasticSearchPercentilesService;
//    }
//
//    //endregion
//}

package com.spotinst.metrics;

import com.spotinst.dropwizard.common.context.BaseAppContext;
import com.spotinst.metrics.bl.model.BlOrganization;
import com.spotinst.metrics.commons.configuration.MetricsConfiguration;
import com.spotinst.metrics.dal.services.elastic.ElasticSearchPercentilesService;
import com.spotinst.metrics.dal.services.elastic.IElasticSearchPercentilesService;
import com.spotinst.metrics.dal.services.elastic.infra.ElasticSearchService;
import com.spotinst.metrics.dal.services.elastic.infra.IElasticSearchService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.math.BigInteger;
import java.util.Map;

public class MetricsAppContext extends BaseAppContext<MetricsConfiguration, BlOrganization> {

    private static MetricsAppContext instance;

    private MetricsAppContext() {
    }

    public static MetricsAppContext getInstance() {

        if (instance == null) {
            synchronized (MetricsAppContext.class) {
                if (instance == null) {
                    instance = new MetricsAppContext();
                }
            }
        }
        return instance;
    }

    //region Members
    private Map<BigInteger, BlOrganization> organizations;
    private ElasticsearchClient elasticClient;
    private IElasticSearchService elasticSearchService;
    private IElasticSearchPercentilesService elasticSearchPercentilesService;

    //endregion

    // Method to get the ElasticSearchService
    public IElasticSearchService getElasticSearchService() {
        if (elasticSearchService == null) {
            // Initialize the elasticSearchService here
            elasticSearchService = new ElasticSearchService(); // Ensure this is a valid instance
        }
        return elasticSearchService;
    }

    // Method to get the ElasticSearchPercentilesService
    public IElasticSearchPercentilesService getElasticSearchPercentilesService() {
        if (elasticSearchPercentilesService == null) {
            // Initialize the elasticSearchPercentilesService here
            elasticSearchPercentilesService = new ElasticSearchPercentilesService(); // Ensure this is a valid instance
        }
        return elasticSearchPercentilesService;
    }

    //region Getters and Setters

    @Override
    public Map<BigInteger, BlOrganization> getOrganizations() {
        return organizations;
    }

    @Override
    public void setOrganizations(Map<BigInteger, BlOrganization> organizations) {
        this.organizations = organizations;
    }

    public ElasticsearchClient getElasticClient() {
        if (elasticClient == null) {
            // Initialize the elasticClient here
            RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();
            ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            elasticClient = new ElasticsearchClient(transport);
        }
        return elasticClient;
    }

    public void setElasticClient(ElasticsearchClient elasticClient) {
        this.elasticClient = elasticClient;
    }

    public void setElasticSearchService(IElasticSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    public void setElasticSearchPercentilesService(IElasticSearchPercentilesService elasticSearchPercentilesService) {
        this.elasticSearchPercentilesService = elasticSearchPercentilesService;
    }

    //endregion
}

