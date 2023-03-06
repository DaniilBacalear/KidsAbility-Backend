package com.kidsability.automation.service;

import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.repository.PractitionerRepository;
import com.kidsability.automation.secret.MailBoxCredentials;
import com.kidsability.automation.util.EmailUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class AccountService {

    private MailBoxCredentials mailBoxCredentials;
    private PractitionerRepository practitionerRepository;
    public AccountService(MailBoxCredentials mailBoxCredentials, PractitionerRepository practitionerRepository) {
        this.mailBoxCredentials = mailBoxCredentials;
        this.practitionerRepository = practitionerRepository;
    }

    public Boolean accountExists(Practitioner practitioner) {
        return practitionerRepository.findByEmail(practitioner.getEmail()) != null;
    }

    public Boolean accountExists(String email) {
        return practitionerRepository.findByEmail(email) != null;
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
            System.out.println(e.getMessage());
        }
    }

    public void changePassword(String sessionToken, String password) {
        Practitioner practitioner = practitionerRepository.findBySessionToken(sessionToken);
        practitioner.setTempPassword(null);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        var encoded = encoder.encode(password);
        practitioner.setPassword(encoded);
        practitionerRepository.save(practitioner);
    }

    public void forgotPassword(String email) {
        if(accountExists(email)) {
            var encoder = new BCryptPasswordEncoder();
            String tempPassword = generateTempPassword();
            StringBuilder sb = new StringBuilder();
            Practitioner practitioner = practitionerRepository.findByEmail(email);
            practitioner.setTempPassword(encoder.encode(tempPassword));
            practitionerRepository.save(practitioner);
            sb.append("You have indicated that you have forgotten your password. The following is your new temporary ")
                    .append(tempPassword)
                    .append(" you can change it after you log in with it.");
            String forgottenPasswordEmailMsg = sb.toString();
            String forgottenPasswordEmailSubject = "Password Reset";
            try {
                EmailUtil.sendEmail(forgottenPasswordEmailMsg, forgottenPasswordEmailSubject, practitioner.getEmail(), mailBoxCredentials);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private String generateTempPassword() {
        int length = 20;
        return RandomStringUtils.random(length, true, true);
    }

}
