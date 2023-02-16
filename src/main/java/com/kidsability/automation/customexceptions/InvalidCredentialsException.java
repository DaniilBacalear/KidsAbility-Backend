package com.kidsability.automation.customexceptions;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException() {
        super("Provided Email or Password is invalid");
    }
}
