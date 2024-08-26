package com.spotinst.metrics.bl.cmds.metadata;

import com.spotinst.commons.cmd.BaseCmd;
import com.spotinst.metrics.bl.model.BlMetric;
import com.spotinst.metrics.bl.model.BlMetricDimension;
import com.spotinst.metrics.bl.model.BlMetricDocument;
import com.spotinst.metrics.bl.model.BlMetricReportRequest;
import com.spotinst.metrics.bl.model.metadata.BlMetricMetadata;
import com.spotinst.metrics.bl.model.metadata.BlNamespaceDimensionPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by zachi.nachshon on 7/9/17.
 */
public class UpdateMetricMetadataCacheCmd extends BaseCmd<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateMetricMetadataCacheCmd.class);

    public UpdateMetricMetadataCacheCmd() {
        super(null, null);
    }

    public void execute(String accountId, BlMetricReportRequest request) {
        List<BlMetricDocument> documents = request.getMetricDocuments();

        if(documents != null && documents.isEmpty() == false) {
            String format = "Scanning metric metadata on [%s] documents under account [%s]...";
            String msg = String.format(format, documents.size(), accountId);
            LOGGER.info(msg);

            documents.forEach(doc -> {

                String                  namespace  = doc.getNamespace();
                List<BlMetric>          metrics    = doc.getMetrics();
                List<BlMetricDimension> dimensions = doc.getDimensions();

                if(dimensions != null && dimensions.isEmpty() == false) {

                    dimensions.forEach(d -> {
                        String                   dimName  = d.getName();
                        BlNamespaceDimensionPair pair     = new BlNamespaceDimensionPair(namespace, dimName);
                        BlMetricMetadata         metadata = new BlMetricMetadata(pair, metrics);

                        MetricMetadataCache.instance.put(accountId, metadata);
                    });
                }
            });
        }
    }
}
