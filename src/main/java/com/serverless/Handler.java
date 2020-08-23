package com.serverless;

import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.serverless.SQSClient;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, ApiGatewayResponse> {
	private static final Logger LOG = LogManager.getLogger(Handler.class);

	@Override
	public ApiGatewayResponse handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		LOG.info("received: {}", input);

		Map<String,String> parameters = input.getQueryStringParameters();
		String currencyFrom = parameters.get("from");
		String currencyTo = parameters.get("to");

		// if (currencyFrom == null || currencyTo == null) {
		// 	Response responseBody = new Response("Missing required parameters", input);
		// 	return ApiGatewayResponse.builder()
		// 			.setStatusCode(400)
		// 			.setObjectBody(responseBody)
		// 			.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
		// 			.build();
		// }


		new SQSClient().sendMessage("Hello");

		LOG.info("Published to sqs");
	
		Response responseBody = new Response("Go Serverless v1.x! Your function executed successfully!", input);
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(responseBody)
				.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
				.build();
	}
}
