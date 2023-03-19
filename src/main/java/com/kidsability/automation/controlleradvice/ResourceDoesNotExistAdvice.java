package com.kidsability.automation.controlleradvice;

import com.kidsability.automation.customexceptions.ResourceDoesNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ResourceDoesNotExistAdvice {
    @ExceptionHandler(ResourceDoesNotExistException.class)
    @ResponseBody
    public ResponseEntity<ErrorData> handleSessionTokenExpired(ResourceDoesNotExistException e) {
        ErrorData errorData = new ErrorData(e.getMessage());
        return new ResponseEntity(errorData, HttpStatus.valueOf(404));
    }
}
