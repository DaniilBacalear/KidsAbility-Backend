package com.kidsability.automation.service;

import com.kidsability.automation.record.Credentials;
import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.record.SessionToken;
import com.kidsability.automation.repository.PractitionerRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class SessionManagementService {
    private PractitionerRepository practitionerRepository;
    public SessionManagementService(PractitionerRepository practitionerRepository) {
        this.practitionerRepository = practitionerRepository;
    }
    public Boolean canLogin(Credentials credentials) {
        Practitioner practitioner = practitionerRepository.findByEmail(credentials.email());
        if(practitioner == null) return false;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = practitioner.getPassword();
        String encodedTempPassword = practitioner.getTempPassword();
        return encoder.matches(credentials.password(), encodedPassword) || encoder.matches(credentials.password(), encodedTempPassword);
    }

    public SessionToken login(Credentials credentials) {
        Practitioner practitioner = practitionerRepository.findByEmail(credentials.email());
        SessionToken sessionToken = generateSessionToken();
        practitioner.setSessionToken(sessionToken.token());
        practitioner.setLastActive(Instant.now());
        practitionerRepository.save(practitioner);
        return sessionToken;
    }

    public Boolean isSessionActive(String sessionToken) {
        Practitioner practitioner = practitionerRepository.findBySessionToken(sessionToken);
        if(practitioner == null) return false;
        int timeoutMinutes = 60;
        long sessionDuration = Duration.between(practitioner.getLastActive(), Instant.now())
                .toMinutes();
        if(sessionDuration > timeoutMinutes) return false;
        practitioner.setLastActive(Instant.now());
        practitionerRepository.save(practitioner);
        return true;
    }

    public Boolean hasAdminPrivileges(String sessionToken) {
        Boolean isSessionActive = isSessionActive(sessionToken);
        if(!isSessionActive) return false;
        Practitioner practitioner = practitionerRepository.findBySessionToken(sessionToken);
        return practitioner.getIsAdmin();
    }

    public void logout(String sessionToken) {
        Practitioner practitioner = practitionerRepository.findBySessionToken(sessionToken);
        practitioner.setSessionToken(null);
        practitionerRepository.save(practitioner);
    }

    private SessionToken generateSessionToken() {
        int length = 60;
        return new SessionToken(RandomStringUtils.random(length, true, true));
    }

}
