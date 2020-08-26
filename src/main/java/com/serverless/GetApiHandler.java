package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.stream.Collectors;

public class GetApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = LogManager.getLogger(GetApiHandler.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        LOG.info("received: {}", input);

        Map<String, String> parameters = input.getQueryStringParameters().entrySet().stream()
                .filter(keyValue -> "from".equals(keyValue.getKey()) || "to".equals(keyValue.getKey()) || "interval".equals(keyValue.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String currencyFrom = parameters.get("from");
        String currencyTo = parameters.get("to");
        String interval = parameters.get("interval");

        if (currencyFrom == null || currencyTo == null) {
            String message = "Missing required parameters";
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(message);
        }

        String data = "";

        //get exchange rates from Redis or fetch directly
        if (interval == null) {
            RedisClient redis = new RedisClient();

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmm");
            String formattedTimestamp = formatter.format(timestamp);

            String key = parameters.get("from") + "TO" + parameters.get("to") + formattedTimestamp;

            data = redis.get(key);

            if (data == null) {
                AlphaVantageClient avClient = new AlphaVantageClient();
                data = avClient.getRates(parameters.get("from"), parameters.get("to"));
                redis.set(key, data);
            }
        } else {
            AlphaVantageClient avClient = new AlphaVantageClient();
            data = avClient.getRatesHistory(parameters.get("from"), parameters.get("to"), parameters.get("interval"));
        }


        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String message = gson.toJson(data);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(message);
    }
}
