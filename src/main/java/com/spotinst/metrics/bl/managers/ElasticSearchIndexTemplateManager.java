package com.spotinst.metrics.bl.managers;

import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.commons.configuration.ElasticIndex;
import com.spotinst.metrics.dal.services.elastic.indexTemplate.ElasticIndexService;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ElasticSearchIndexTemplateManager {
    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchIndexTemplateManager.class);

    private final ElasticIndexService elasticTemplateService;
    //endregion

    //region Constructor
    public ElasticSearchIndexTemplateManager() {
        this.elasticTemplateService = new ElasticIndexService();
    }
    //endregion

    //region Public Methods
    public void start() throws Exception {
        LOGGER.info("Starting Elasticsearch template creation...");
        createTemplatesFromResources();
        LOGGER.info("Elasticsearch template creation completed.");
    }
    //endregion

    //region Private Methods
    private void createTemplatesFromResources() {
        try {
            // Get all template files from resources
            List<String> templateFilesNames =
                    MetricsAppContext.getInstance().getConfiguration().getElastic().getIndexes().stream()
                                     .map(ElasticIndex::getTemplateFileName).toList();

            LOGGER.info("Found {} template files", templateFilesNames.size());

            for (String templateName : templateFilesNames) {
                String templateJson = resolveTemplateJsonFromTemplateName(templateName);

                try {
                    boolean templateExists = elasticTemplateService.checkTemplateExists(templateName);

                    if (BooleanUtils.isFalse(templateExists)) {
                        boolean templateCreatedSuccessfully =
                                elasticTemplateService.createIndexTemplate(templateName, templateJson);

                        if (templateCreatedSuccessfully) {
                            LOGGER.info("Created new index template: {}", templateName);
                        }
                        else {
                            LOGGER.warn("Failed to create index template: {}", templateName);
                        }
                    }
                    else {
                        LOGGER.info("Index template {} already exists.", templateName);
                    }
                }
                catch (Exception e) {
                    LOGGER.error("Failed to process index template: {}", templateName, e);
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Error managing Elasticsearch index templates", e);
            throw new RuntimeException("Failed to manage Elasticsearch index templates", e);
        }
    }

    private String resolveTemplateJsonFromTemplateName(String templateFileName) throws IOException {
        String resourcePath = String.format("indexTemplates/%s", templateFileName);

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {

            if (inputStream == null) {
                throw new IOException("Resource not found: {}" + resourcePath);
            }

            return new String(inputStream.readAllBytes());
        }
    }
    //endregion
}