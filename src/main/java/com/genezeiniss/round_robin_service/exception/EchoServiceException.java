package com.genezeiniss.round_robin_service.exception;

import org.springframework.http.HttpStatusCode;

public class EchoServiceException extends RuntimeException {

    public EchoServiceException(String instanceUrl, HttpStatusCode statusCode) {
        super("Echo service at %s responded with %s".formatted(instanceUrl, statusCode));
    }

    public EchoServiceException(String instanceUrl, String message) {
        super("Echo service at %s %s".formatted(instanceUrl, message));
    }
}
