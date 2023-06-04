package com.kidsability.automation.service;
import com.kidsability.automation.customexceptions.ClientAlreadyExistsException;
import com.kidsability.automation.model.Client;
import com.kidsability.automation.repository.BehaviourRepository;
import com.kidsability.automation.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final SharePointService sharePointService;
    private final BehaviourService behaviourService;

    public ClientService(ClientRepository clientRepository, SharePointService sharePointService, BehaviourService behaviourService) {
        this.clientRepository = clientRepository;
        this.sharePointService = sharePointService;
        this.behaviourService = behaviourService;

    }
    public void createClient(String kidsAbilityId) throws ClientAlreadyExistsException, ExecutionException, InterruptedException {
        if(clientExists(kidsAbilityId)) throw new ClientAlreadyExistsException();
        var client = Client.builder()
                .kidsAbilityId(kidsAbilityId)
                .build();
        var clientSharePointRootFolder = sharePointService.createClientFolders(client);
        client.setSharePointRootId(clientSharePointRootFolder.id);
        clientRepository.save(client);
        behaviourService.initBehaviour(client);
    }

    public Boolean clientExists(String kidsAbilityId) {
        return clientRepository.findByKidsAbilityId(kidsAbilityId) != null;
    }

    public Client getClient(String kidsAbilityId) {
        return clientRepository.findByKidsAbilityId(kidsAbilityId);
    }


    public List<Client> getClients() {
        return clientRepository.findAllByQuery();
    }
}
