package com.kidsability.automation.customexceptions;

public class ResourceDoesNotExistException extends RuntimeException {
    public ResourceDoesNotExistException() {
        super("The resource for which the api was called does not exist.");
    }
}
