package com.serverless;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

class SQSClient {
    private static final String QUEUE = System.getenv("RATES_QUEUE");
    private static final String REGION = defaultIfBlank(System.getenv("REGION"), "us-east-1");
    private AmazonSQS sqs;

    SQSClient() {
        this.sqs = AmazonSQSClientBuilder.standard()
                .withRegion(REGION)
                .build();
    }

    void sendMessage(String message) {
        SendMessageRequest request = new SendMessageRequest()
                .withQueueUrl(QUEUE)
                .withMessageBody(message)
                .withDelaySeconds(5);

        this.sqs.sendMessage(request);
    }
}
