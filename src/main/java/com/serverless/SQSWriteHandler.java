package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class SQSWriteHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = LogManager.getLogger(SQSWriteHandler.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        LOG.info("received: {}", input);

        Map<String, String> parameters = input.getQueryStringParameters();
        String currencyFrom = parameters.get("from");
        String currencyTo = parameters.get("to");

        if (currencyFrom == null || currencyTo == null) {
            String message = "Missing required parameters";
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(message);
        }

        new SQSClient().sendMessage("Hello");
        LOG.info("Published to sqs");

        String message = "All is well";
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(message);
    }
}
