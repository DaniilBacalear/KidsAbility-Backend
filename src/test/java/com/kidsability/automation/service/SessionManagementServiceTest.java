package com.kidsability.automation.service;

import com.kidsability.automation.record.Credentials;
import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.repository.PractitionerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SessionManagementServiceTest {
    @MockBean
    private PractitionerRepository practitionerRepository;

    private Practitioner practitioner;

    private SessionManagementService sessionManagementService;



    @BeforeEach
    void setUp() {
        sessionManagementService = new SessionManagementService(practitionerRepository);
        var encoder = new BCryptPasswordEncoder();
        practitioner = Practitioner.builder()
                .email("test@gmail.com")
                .password(encoder.encode("1234"))
                .id(1l)
                .build();

    }

    @Test
    void whenLogin_thenGenerate60CharSessionToken() {
        var credentials = new Credentials("test@gmail.com", "1234");
        Mockito.when(practitionerRepository
                .findByEmail("test@gmail.com"))
                .thenReturn(practitioner);
        var sessionToken = sessionManagementService.login(credentials);
        assertEquals(60, sessionToken.sessionToken().length());
    }

    @Test
    void whenValidCredentials_thenCanLoginReturnsTrue() {
        var credentials = new Credentials("test@gmail.com", "1234");
        Mockito.when(practitionerRepository.findByEmail("test@gmail.com"))
                .thenReturn(practitioner);
        assertTrue(sessionManagementService.canLogin(credentials));
    }

    @Test
    void whenInValidPassword_thenCanLoginReturnsFalse() {
        var credentials = new Credentials("test@gmail.com", "12345");
        Mockito.when(practitionerRepository.findByEmail("test@gmail.com"))
                .thenReturn(practitioner);
        assertFalse(sessionManagementService.canLogin(credentials));
    }

    @Test
    void whenInValidEmail_thenCanLoginReturnsFalse() {
        var credentials = new Credentials("test2@gmail.com", "1234");
        Mockito.when(practitionerRepository.findByEmail("test@gmail.com"))
                .thenReturn(practitioner);
        assertFalse(sessionManagementService.canLogin(credentials));
    }

    @Test
    void whenSessionOverTimeOut_isActiveSessionReturnsFalse() {
        var practitioner2 = Practitioner.builder()
                .sessionToken("abcd")
                .lastActive(Instant.now().minusSeconds(60 * 90))
                .build();
        Mockito.when(practitionerRepository.findBySessionToken("abcd"))
                .thenReturn(practitioner2);
        assertFalse(sessionManagementService.isSessionActive("abcd"));
    }

    @Test
    void whenSessionWithinTimeOut_isActiveSessionReturnsTrue() {
        var practitioner2 = Practitioner.builder()
                .sessionToken("abcd")
                .lastActive(Instant.now().minusSeconds(60 * 30))
                .build();
        Mockito.when(practitionerRepository.findBySessionToken("abcd"))
                .thenReturn(practitioner2);
        assertTrue(sessionManagementService.isSessionActive("abcd"));
    }


}