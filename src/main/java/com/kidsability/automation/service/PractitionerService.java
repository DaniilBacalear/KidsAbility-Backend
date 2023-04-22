package com.kidsability.automation.service;

import com.kidsability.automation.model.Client;
import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.repository.PractitionerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class PractitionerService {
    private PractitionerRepository practitionerRepository;
    public PractitionerService(PractitionerRepository practitionerRepository) {
        this.practitionerRepository = practitionerRepository;
    }

    public Practitioner getPractitioner(String sessionToken) {
        return practitionerRepository.findBySessionToken(sessionToken);
    }

    public Practitioner savePractitioner(Practitioner practitioner) {
        return practitionerRepository.save(practitioner);
    }

}
