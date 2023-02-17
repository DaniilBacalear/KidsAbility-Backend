package com.kidsability.automation.customexceptions;

public class SessionTokenExpiredException extends RuntimeException {
    public SessionTokenExpiredException() {
        super("The session token used has expired");
    }
}
