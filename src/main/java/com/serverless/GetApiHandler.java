package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class GetApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = LogManager.getLogger(GetApiHandler.class);

    private APIGatewayProxyResponseEvent respondBadRequest() {
        String message = "Missing required parameters";
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withHeaders(new HashMap<String, String>() {{
                    put("Content-Type", "application/json");
                    put("Access-Control-Allow-Origin", "*");
                    put("Access-Control-Allow-Credentials", "true");
                }})
                .withBody(message);
    }

    private APIGatewayProxyResponseEvent respondOk(String data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String exchangeRates = JsonParser.parseString(data)
                .getAsJsonObject()
                .get("Realtime Currency Exchange Rate")
                .getAsJsonObject()
                .get("5. Exchange Rate")
                .getAsString();

        JsonObject serialized = new JsonObject();
        serialized.addProperty("exchangeRates", Float.valueOf(exchangeRates));

        String message = gson.toJson(serialized);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(new HashMap<String, String>() {{
                    put("Content-Type", "application/json");
                    put("Access-Control-Allow-Origin", "*");
                    put("Access-Control-Allow-Credentials", "true");
                }})
                .withBody(message);
    }

    private APIGatewayProxyResponseEvent respondOkHistoryData(String data, String interval) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String seriesKey = "";
        if ("monthly".equals(interval)) {
            seriesKey = "Time Series FX (Daily)";
        } else if ("weekly".equals(interval)) {
            seriesKey = "Time Series FX (60min)";
        } else if ("daily".equals(interval)) {
            seriesKey = "Time Series FX (5min)";
        }

        JsonObject series = JsonParser.parseString(data)
                .getAsJsonObject()
                .get(seriesKey)
                .getAsJsonObject();

        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> myMap = gson.fromJson("{'k1':'apple','k2':'orange'}", type);

        Map<Object, Object> seriesMap = series.entrySet()
                .stream()
                .map(entry -> {
                    Map<String, String> valueMap = gson.fromJson(entry.getValue(), type);
                    Map<String, String> newValueMap = valueMap.entrySet().stream()
                            .map(oldEntry -> {
                                String key = oldEntry.getKey().replaceAll("[1-4]\\. ", "");
                                return new AbstractMap.SimpleEntry<String, String>(key, oldEntry.getValue());
                            })
                            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
                    return new AbstractMap.SimpleEntry<String, Object>(entry.getKey(), newValueMap);
                })
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        String message = gson.toJson(seriesMap);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(new HashMap<String, String>() {{
                    put("Content-Type", "application/json");
                    put("Access-Control-Allow-Origin", "*");
                    put("Access-Control-Allow-Credentials", "true");
                }})
                .withBody(message);
    }

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
            return respondBadRequest();
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
            return respondOk(data);
        } else {
            AlphaVantageClient avClient = new AlphaVantageClient();
            data = avClient.getRatesHistory(parameters.get("from"), parameters.get("to"), parameters.get("interval"));
            return respondOkHistoryData(data, interval);
        }
    }
}
