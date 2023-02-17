package com.kidsability.automation.controlleradvice;

import com.kidsability.automation.customexceptions.InsufficientPrivilegesException;
import com.kidsability.automation.customexceptions.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class InsufficientPrivilegesAdvice {
    @ExceptionHandler(InsufficientPrivilegesException.class)
    @ResponseBody
    public ResponseEntity<ErrorData> handleInsufficientPrivileges(InsufficientPrivilegesException e) {
        ErrorData errorData = new ErrorData(e.getMessage());
        return new ResponseEntity(errorData, HttpStatus.FORBIDDEN);
    }
}
