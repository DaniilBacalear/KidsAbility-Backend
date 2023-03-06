package com.kidsability.automation.controller;

import com.kidsability.automation.customexceptions.InvalidCredentialsException;
import com.kidsability.automation.customexceptions.SessionTokenExpiredException;
import com.kidsability.automation.record.Credentials;
import com.kidsability.automation.record.SessionToken;
import com.kidsability.automation.service.SessionManagementService;
import org.springframework.web.bind.annotation.*;

@RestController
public class SessionController {
    private SessionManagementService sessionManagementService;
    public SessionController(SessionManagementService sessionManagementService) {
        this.sessionManagementService = sessionManagementService;
    }
    @PostMapping("/login")
    SessionToken login(@RequestBody Credentials credentials) {
        if(sessionManagementService.canLogin(credentials)) {
            return sessionManagementService.login(credentials);
        }
        throw new InvalidCredentialsException();
    }

    @PostMapping("/logout")
    void logout(@RequestBody SessionToken sessionToken) {
        if(!sessionManagementService.isSessionActive(sessionToken.token())) throw new SessionTokenExpiredException();
        sessionManagementService.logout(sessionToken.token());
    }


}
