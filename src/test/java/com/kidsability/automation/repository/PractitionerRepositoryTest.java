package com.kidsability.automation.repository;

import com.kidsability.automation.model.Practitioner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
//@DataJpaTest
//@ActiveProfiles("test")
class PractitionerRepositoryTest {
    @Autowired
    private PractitionerRepository practitionerRepository;

    @Test
    public void savePractitioner() {
        var encoder = new BCryptPasswordEncoder();
        Practitioner practitioner = Practitioner.builder()
                .firstName("Daniil")
                .lastName("Bacalear")
                .email("test@gmail.com")
                .isAdmin(true)
                .password(encoder.encode("1234"))
                .sessionToken("abc")
                .lastActive(Instant.now())
                .build();
        practitionerRepository.save(practitioner);
        assertEquals(1, practitionerRepository.count());
    }

    @Test
    public void findNull() {
        Practitioner practitioner1 = practitionerRepository.findByEmail("test2@gmail.com");
        assertNotNull(practitioner1);
        Practitioner practitioner2 = practitionerRepository.findByEmail("bs");
        assertNull(practitioner2);
        Practitioner practitioner3 = practitionerRepository.findBySessionToken("abc");
        assertNotNull(practitioner3);
    }

}