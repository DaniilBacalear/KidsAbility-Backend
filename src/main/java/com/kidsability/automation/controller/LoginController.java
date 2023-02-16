package com.kidsability.automation.controller;

import com.kidsability.automation.customexceptions.InvalidCredentialsException;
import com.kidsability.automation.record.Credentials;
import com.kidsability.automation.record.SessionToken;
import com.kidsability.automation.service.SessionManagementService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {
    private SessionManagementService sessionManagementService;
    public LoginController(SessionManagementService sessionManagementService) {
        this.sessionManagementService = sessionManagementService;
    }
    @PostMapping
    SessionToken login(@RequestBody Credentials credentials) {
        if(sessionManagementService.canLogin(credentials)) {
            return sessionManagementService.login(credentials);
        }
        throw new InvalidCredentialsException();
    }

}
