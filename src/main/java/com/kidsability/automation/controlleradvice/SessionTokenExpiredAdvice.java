package com.kidsability.automation.controlleradvice;

import com.kidsability.automation.customexceptions.InsufficientPrivilegesException;
import com.kidsability.automation.customexceptions.SessionTokenExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

public class SessionTokenExpiredAdvice {
    @ExceptionHandler(SessionTokenExpiredException.class)
    @ResponseBody
    public ResponseEntity<ErrorData> handleSessionTokenExpired(SessionTokenExpiredException e) {
        ErrorData errorData = new ErrorData(e.getMessage());
        return new ResponseEntity(errorData, HttpStatus.valueOf(440));
    }
}
