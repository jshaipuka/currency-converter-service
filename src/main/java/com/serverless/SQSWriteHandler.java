package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.stream.Collectors;

public class SQSWriteHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = LogManager.getLogger(SQSWriteHandler.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        LOG.info("received: {}", input);

        Map<String, String> parameters = input.getQueryStringParameters().entrySet().stream()
                .filter(keyValue -> "from".equals(keyValue.getKey()) || "to".equals(keyValue.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String currencyFrom = parameters.get("from");
        String currencyTo = parameters.get("to");

        if (currencyFrom == null || currencyTo == null) {
            String message = "Missing required parameters";
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(message);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String message = gson.toJson(parameters);
        new SQSClient().sendMessage(message);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200);
    }
}
