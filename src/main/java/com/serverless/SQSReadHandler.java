package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class SQSReadHandler implements RequestHandler<SQSEvent, Void> {

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        List<String> messages = getSQSMessages(event);

        messages.forEach(System.out::println);

        return null;
    }

    private static List<String> getSQSMessages(SQSEvent event) {
        return event.getRecords()
                .stream()
                .map(SQSMessage::getBody)
                .collect(toList());
    }
}