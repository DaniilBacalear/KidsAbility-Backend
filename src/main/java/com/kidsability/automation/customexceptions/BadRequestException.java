package com.kidsability.automation.customexceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException() {
        super("Provided request is invalid");
    }

    public BadRequestException(String msg) {
        super(msg);
    }
}
