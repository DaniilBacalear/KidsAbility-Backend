package com.kidsability.automation.customexceptions;

public class ClientAlreadyExistsException extends RuntimeException{
    public ClientAlreadyExistsException() {
        super("The client already exists.");
    }
}
