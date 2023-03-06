package com.kidsability.automation.customexceptions;

public class PractitionerAlreadyExistsException extends RuntimeException{
    public PractitionerAlreadyExistsException() {
        super("The practitioner you are trying to register already exists");
    }
}
