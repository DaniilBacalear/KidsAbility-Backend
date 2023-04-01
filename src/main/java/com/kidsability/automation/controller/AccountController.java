package com.kidsability.automation.controller;

import com.kidsability.automation.customexceptions.InsufficientPrivilegesException;
import com.kidsability.automation.customexceptions.PractitionerAlreadyExistsException;
import com.kidsability.automation.customexceptions.SessionTokenExpiredException;
import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.record.Credentials;
import com.kidsability.automation.service.AccountService;
import com.kidsability.automation.service.SessionManagementService;
import org.springframework.web.bind.annotation.*;

@RestController()
@CrossOrigin
public class AccountController {

    private AccountService accountService;
    private SessionManagementService sessionManagementService;
    public AccountController(AccountService accountService, SessionManagementService sessionManagementService) {
        this.accountService = accountService;
        this.sessionManagementService = sessionManagementService;
    }

    @PostMapping("account/register")
    void register(@RequestBody Practitioner practitioner, @RequestHeader("sessionToken") String sessionToken) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        if(!sessionManagementService.hasAdminPrivileges(sessionToken)) throw new InsufficientPrivilegesException();
        if(accountService.accountExists(practitioner)) throw new PractitionerAlreadyExistsException();
        accountService.registerPractitioner(practitioner);
    }

    @PostMapping("account/change-password")
    void changePassword(@RequestBody Credentials credentials, @RequestHeader("sessionToken") String sessionToken) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        accountService.changePassword(sessionToken, credentials.password());
    }

    @PostMapping("account/forgot-password")
    void forgotPassword(@RequestBody Credentials credentials) {
        accountService.forgotPassword(credentials.email());
    }

}
