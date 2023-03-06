package com.kidsability.automation.data;

import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.repository.PractitionerRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {
    private PractitionerRepository practitionerRepository;
    public DataLoader(PractitionerRepository practitionerRepository) {
        this.practitionerRepository = practitionerRepository;
    }
    public void run(ApplicationArguments args) {
        populatePractitioners();
    }

    private void populatePractitioners() {
        try {
            var encoder = new BCryptPasswordEncoder();
            Practitioner practitioner = Practitioner.builder()
                    .password(encoder.encode("1234"))
                    .email("dbacalea@uwaterloo.ca")
                    .isAdmin(true)
                    .firstName("Daniil")
                    .lastName("Bacalear")
                    .build();
            practitionerRepository.save(practitioner);
        }
        catch (Exception e) {

        }
    }


}
