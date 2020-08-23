package com.serverless;

import java.util.Map;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class Response {

	private final String message;
	private final APIGatewayProxyRequestEvent input;

	public Response(String message, APIGatewayProxyRequestEvent input) {
		this.message = message;
		this.input = input;
	}

	public String getMessage() {
		return this.message;
	}

	public APIGatewayProxyRequestEvent getInput() {
		return this.input;
	}
}
