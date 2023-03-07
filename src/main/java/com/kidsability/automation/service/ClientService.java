package com.kidsability.automation.service;

import com.kidsability.automation.customexceptions.ClientAlreadyExistsException;
import com.kidsability.automation.model.Client;
import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.repository.ClientRepository;
import com.kidsability.automation.repository.PractitionerRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ClientService {
    private ClientRepository clientRepository;
    private PractitionerRepository practitionerRepository;

    public ClientService(ClientRepository clientRepository, PractitionerRepository practitionerRepository) {
        this.clientRepository = clientRepository;
        this.practitionerRepository = practitionerRepository;
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

    public Client getClient(String kidsabilityId) {
        return clientRepository.findByKidsabilityId(kidsabilityId);
    }

    public void addToPractitioner(Client client, String sessionToken) {
        Practitioner practitioner = practitionerRepository.findBySessionToken(sessionToken);
        if(practitioner.getClients() == null) {
            practitioner.addClient(client);
        }
        else {
            for(Client c : practitioner.getClients()) {
                if(c.getKidsabilityId().equals(client.getKidsabilityId())) return;
            }
            practitioner.addClient(client);
        }
        practitionerRepository.save(practitioner);
    }
    public Set<Client> getClients(String sessionToken) {
        Practitioner practitioner = practitionerRepository.findBySessionToken(sessionToken);
        return practitioner.getClients();
    }
}
