package com.genezeiniss.round_robin_service.exception;

public class NoHealthyInstancesException extends RuntimeException {

    public NoHealthyInstancesException(String message) {
        super(message);
    }
}
