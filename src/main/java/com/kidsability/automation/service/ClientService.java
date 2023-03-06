package com.kidsability.automation.service;

import com.kidsability.automation.customexceptions.ClientAlreadyExistsException;
import com.kidsability.automation.model.Client;
import com.kidsability.automation.repository.ClientRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
    private ClientRepository clientRepository;
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }
    public Client createClient(String kidsabilityId) throws ClientAlreadyExistsException {
        if(clientExists(kidsabilityId)) throw new ClientAlreadyExistsException();
        Client client = Client.builder()
                .kidsabilityId(kidsabilityId)
                .build();
        return clientRepository.save(client);
    }

    public Boolean clientExists(String kidsabilityId) {
        return clientRepository.findByKidsabilityId(kidsabilityId) != null;
    }
}
