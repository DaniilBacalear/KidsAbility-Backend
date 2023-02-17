package com.kidsability.automation.service;

import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.record.Credentials;
import com.kidsability.automation.record.SessionToken;
import com.kidsability.automation.repository.PractitionerRepository;
import com.kidsability.automation.secret.MailBoxCredentials;
import com.kidsability.automation.util.EmailUtil;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class AccountService {

    private MailBoxCredentials mailBoxCredentials;
    private PractitionerRepository practitionerRepository;
    public AccountService(MailBoxCredentials mailBoxCredentials, PractitionerRepository practitionerRepository) {
        this.mailBoxCredentials = mailBoxCredentials;
        this.practitionerRepository = practitionerRepository;
    }

    public void registerPractitioner(Practitioner practitioner) {
        String tempPassword = generateTempPassword();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Practitioner toSave = Practitioner.builder()
                .tempPassword(encoder.encode(tempPassword))
                .firstName(practitioner.getFirstName())
                .lastName(practitioner.getLastName())
                .email(practitioner.getEmail())
                .isAdmin(practitioner.getIsAdmin())
                .build();
        practitionerRepository.save(toSave);
        String registrationEmailMsg = "Your temporary password is: " + tempPassword + " you can change it after you login.";
        String registrationEmailSubject = "Temporary Password";
        try {
            EmailUtil.sendEmail(registrationEmailMsg, registrationEmailSubject, practitioner.getEmail(),mailBoxCredentials);
        }
        catch (Exception e) {
            System.out.printf(e.getMessage());
        }
    }

    private String generateTempPassword() {
        int length = 20;
        return RandomStringUtils.random(length, true, true);
    }

}
