package com.kidsability.automation.controller;

import com.kidsability.automation.customexceptions.ClientAlreadyExistsException;
import com.kidsability.automation.customexceptions.SessionTokenExpiredException;
import com.kidsability.automation.model.Client;
import com.kidsability.automation.service.ClientService;
import com.kidsability.automation.service.SessionManagementService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientController {
    private ClientService clientService;
    private SessionManagementService sessionManagementService;
    public ClientController(ClientService clientService, SessionManagementService sessionManagementService) {
        this.clientService = clientService;
        this.sessionManagementService = sessionManagementService;
    }
    @PostMapping("/client")
    public void createClient(@RequestBody Client client, @RequestHeader("sessionToken") String sessionToken) throws ClientAlreadyExistsException {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        clientService.createClient(client.getKidsabilityId());
    }
}
