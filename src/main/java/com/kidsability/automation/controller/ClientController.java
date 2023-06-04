package com.kidsability.automation.controller;

import com.kidsability.automation.customexceptions.ClientAlreadyExistsException;
import com.kidsability.automation.customexceptions.ResourceDoesNotExistException;
import com.kidsability.automation.customexceptions.SessionTokenExpiredException;
import com.kidsability.automation.model.Behaviour;
import com.kidsability.automation.model.BehaviourSession;
import com.kidsability.automation.model.Client;
import com.kidsability.automation.record.BehaviourExcelLinkRecord;
import com.kidsability.automation.service.BehaviourService;
import com.kidsability.automation.service.ClientService;
import com.kidsability.automation.service.SessionManagementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@CrossOrigin
public class ClientController {
    private final ClientService clientService;
    private final SessionManagementService sessionManagementService;
    private final BehaviourService behaviourService;
    public ClientController(ClientService clientService, SessionManagementService sessionManagementService, BehaviourService behaviourService) {
        this.clientService = clientService;
        this.sessionManagementService = sessionManagementService;
        this.behaviourService = behaviourService;
    }
    @PostMapping("/client")
    public void createClient(@RequestBody Client client, @RequestHeader("sessionToken") String sessionToken) throws ClientAlreadyExistsException, ExecutionException, InterruptedException {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        clientService.createClient(client.getKidsAbilityId());
    }

    @GetMapping("/client")
    public List<Client> getClients(@RequestHeader("sessionToken") String sessionToken) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        return clientService.getClients();
    }

    @GetMapping("client/{kidsAbilityId}/behaviour/active")
    public BehaviourSession getActiveBehaviourSession(@RequestHeader("sessionToken") String sessionToken, @PathVariable String kidsAbilityId) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        Client client = clientService.getClient(kidsAbilityId);
        if(client == null) throw new ResourceDoesNotExistException();
        Behaviour behaviour = client.getBehaviour();
        BehaviourSession activeBehaviourSession = behaviourService.getActiveBehaviourSession(behaviour);
        return activeBehaviourSession;
    }

    @PostMapping("client/{kidsAbilityId}/behaviour/active")
    public void saveActiveBehaviourSession(@RequestHeader("sessionToken") String sessionToken, @PathVariable String kidsAbilityId, @RequestBody BehaviourSession updatedBehaviourSession) throws ExecutionException, InterruptedException {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        Client client = clientService.getClient(kidsAbilityId);
        if(client == null) throw new ResourceDoesNotExistException();
        behaviourService.saveActiveBehaviourSession(client, updatedBehaviourSession);
    }

    @GetMapping("client/{kidsAbilityId}/behaviour/excel")
    public BehaviourExcelLinkRecord getBehaviourExcelLink(@RequestHeader("sessionToken") String sessionToken, @PathVariable String kidsAbilityId) throws ExecutionException, InterruptedException {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        Client client = clientService.getClient(kidsAbilityId);
        if(client == null) throw new ResourceDoesNotExistException();
        Behaviour behaviour = client.getBehaviour();
        var excelLink =  behaviourService.getExcelEmbeddableLink(behaviour);
        return BehaviourExcelLinkRecord
                .builder()
                .excelLink(excelLink)
                .build();
    }

}
