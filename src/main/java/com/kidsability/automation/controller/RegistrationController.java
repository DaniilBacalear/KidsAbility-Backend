package com.kidsability.automation.controller;

import com.kidsability.automation.customexceptions.InsufficientPrivilegesException;
import com.kidsability.automation.customexceptions.SessionTokenExpiredException;
import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.service.AccountService;
import com.kidsability.automation.service.SessionManagementService;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/register")
public class RegistrationController {

    private AccountService accountService;
    private SessionManagementService sessionManagementService;
    public RegistrationController(AccountService accountService, SessionManagementService sessionManagementService) {
        this.accountService = accountService;
        this.sessionManagementService = sessionManagementService;
    }

    @PostMapping
    void register(@RequestBody Practitioner practitioner, @RequestHeader("sessionToken") String sessionToken) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        if(!sessionManagementService.hasAdminPrivileges(sessionToken)) throw new InsufficientPrivilegesException();
        accountService.registerPractitioner(practitioner);
    }

}
