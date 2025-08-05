package com.spotinst.metrics.dal.services.elastic.indexTemplate;

import co.elastic.clients.elasticsearch.indices.*;
import com.spotinst.metrics.dal.services.elastic.infra.BaseElasticSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;

public class ElasticIndexService extends BaseElasticSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticIndexService.class);

    //endregion
    //region Private Methods

    public boolean checkTemplateExists(String templateName) {
        boolean retVal = false;

        try {
            boolean exists = elasticsearchClient.indices().existsIndexTemplate(
                    ExistsIndexTemplateRequest.of(builder -> builder.name(templateName))).value();
            LOGGER.debug("Template '{}' exists: {}", templateName, exists);
            retVal = exists;
        }
        catch (Exception e) {
            LOGGER.error("Error checking template existence: {}", templateName, e);
        }

        return retVal;
    }

    public boolean createIndexTemplate(String templateName, String templateJson) throws IOException {
        boolean retVal = false;

        LOGGER.debug("Template JSON for '{}': {}", templateName, templateJson);

        try {
            // Simply pass the entire JSON directly to Elasticsearch
            PutIndexTemplateResponse putIndexTemplateResponse = elasticsearchClient.indices().putIndexTemplate(
                    PutIndexTemplateRequest.of(
                            builder -> builder.name(templateName).withJson(new StringReader(templateJson))));

            if (putIndexTemplateResponse.acknowledged()) {
                retVal = true;
                LOGGER.info("Successfully created index template: {}", templateName);
            }
            else {
                LOGGER.warn("Failed to create index template: {}", templateName);
            }
        }
        catch (Exception e) {
            LOGGER.error("Error creating index template: {}", templateName, e);
            throw new RuntimeException("Failed to create index template: " + templateName, e);
        }

        return retVal;
    }
}