package com.kidsability.automation.controlleradvice;

import com.kidsability.automation.customexceptions.BadRequestException;
import com.kidsability.automation.customexceptions.ClientAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class BadRequestAdvice {
    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    public ResponseEntity<ErrorData> handleSessionTokenExpired(BadRequestException e) {
        ErrorData errorData = new ErrorData(e.getMessage());
        return new ResponseEntity(errorData, HttpStatus.BAD_REQUEST);
    }
}
