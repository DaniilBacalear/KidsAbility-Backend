package com.kidsability.automation.controller;

import com.kidsability.automation.customexceptions.ClientAlreadyExistsException;
import com.kidsability.automation.customexceptions.SessionTokenExpiredException;
import com.kidsability.automation.model.Client;
import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.service.ClientService;
import com.kidsability.automation.service.SessionManagementService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

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

    @GetMapping("/client")
    public Set<Client> getClients(@RequestHeader("sessionToken") String sessionToken) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        return clientService.getClients(sessionToken);
    }

    @PostMapping("/client/program")
    public void createProgram() {

    }
}
