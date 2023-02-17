package com.kidsability.automation.customexceptions;

public class InsufficientPrivilegesException extends RuntimeException{
    public InsufficientPrivilegesException() {
        super("The user does not have the necessary privileges to perform this action.");
    }
}
