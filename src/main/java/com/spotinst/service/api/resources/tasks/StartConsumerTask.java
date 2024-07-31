package com.spotinst.service.api.resources.tasks;

import com.spotinst.dropwizard.common.constants.ServiceConstants;
import com.spotinst.messaging.clients.consumer.IMessagingConsumer;
import com.spotinst.service.SampleAppContext;
import io.dropwizard.servlets.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.Produces;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by zachi.nachshon on 12/12/17.
 */
@Produces(ServiceConstants.REST.CONTENT_TYPE_APPLICATION_JSON)
public class StartConsumerTask extends Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartConsumerTask.class);

    private static final String STOP_CONSUMER_URL = "sample/consumer/start";

    public StartConsumerTask() {
        super(STOP_CONSUMER_URL);
    }

    @Override
    @Produces(ServiceConstants.REST.CONTENT_TYPE_APPLICATION_JSON)
    public void execute(Map<String, List<String>> parameters, PrintWriter output) {
        LOGGER.info("Starting SQS message consumption...");

        List<String> autoAckParam = parameters.get("autoAck");

        IMessagingConsumer sqsConsumer = SampleAppContext.getInstance().getSqsConsumer();

        String result;
        try {
            if(sqsConsumer != null) {
                Boolean autoAck = false;
                if(autoAckParam != null && autoAckParam.isEmpty() == false) {
                    autoAck = Boolean.valueOf(autoAckParam.toString());
                }
                sqsConsumer.consume(autoAck);
                result = "Successfully started SQS message consumer";
            } else {
                result = "Message consumer was not initialized, cannot start";
            }
            LOGGER.info(result);

        } catch (Exception e) {
            String format = "Failed starting message consumer. Error: %s";
            result = String.format(format, e.getMessage());
        }

        output.println(result);
    }
}
