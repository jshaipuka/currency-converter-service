package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class SQSReadHandler implements RequestHandler<SQSEvent, Void> {

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        List<String> messages = getSQSMessages(event);

        messages.forEach(System.out::println);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmm");
        String formattedTimestamp = formatter.format(timestamp);

        RedisClient redis = new RedisClient();

        messages.forEach(message -> {
            Map<String, String> params = gson.fromJson(message, type);
            String key = params.get("from") + params.get("to") + formattedTimestamp;
            String value = "api value response for " + params.get("from") + params.get("to");

            if (redis.get(key) == null) {
                redis.set(key, value);
            }
        });

        return null;
    }

    private static List<String> getSQSMessages(SQSEvent event) {
        return event.getRecords()
                .stream()
                .map(SQSMessage::getBody)
                .collect(toList());
    }
}