package com.kidsability.automation.service;

import com.kidsability.automation.customexceptions.ClientAlreadyExistsException;
import com.kidsability.automation.model.Client;
import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.repository.ClientRepository;
import com.kidsability.automation.repository.PractitionerRepository;
import com.microsoft.graph.models.DriveItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ClientService {
    private ClientRepository clientRepository;
    private SharePointService sharePointService;

    public ClientService(ClientRepository clientRepository, SharePointService sharePointService) {
        this.clientRepository = clientRepository;
        this.sharePointService = sharePointService;
    }
    public Client createClient(String kidsAbilityId) throws ClientAlreadyExistsException {
        if(clientExists(kidsAbilityId)) throw new ClientAlreadyExistsException();
        var client = Client.builder()
                .kidsAbilityId(kidsAbilityId)
                .build();
        var clientSharePointRootFolder = sharePointService.createClientFolders(client);
        client.setSharePointRootId(clientSharePointRootFolder.id);
        System.out.println(client);
        return clientRepository.save(client);
    }

    public Boolean clientExists(String kidsAbilityId) {
        return clientRepository.findByKidsAbilityId(kidsAbilityId) != null;
    }

    public Client getClient(String kidsAbilityId) {
        return clientRepository.findByKidsAbilityId(kidsAbilityId);
    }


    public List<Client> getClients() {
        return clientRepository.findAll();
    }
}
