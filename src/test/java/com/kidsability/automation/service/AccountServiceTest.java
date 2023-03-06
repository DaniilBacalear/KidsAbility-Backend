package com.kidsability.automation.service;

import com.kidsability.automation.repository.PractitionerRepository;
import com.kidsability.automation.secret.MailBoxCredentials;
import com.sun.source.tree.AssertTree;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AccountServiceTest {
    @Autowired
    private AccountService accountService;
    @Autowired
    private PractitionerRepository practitionerRepository;

    @Test
    void temp() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches("1234", "$2a$10$MnUYp0cgSFahVpYh9H.Htu4V4OiUeiJJCl9yQfrGK75e2y4B4GjLW"));
    }

    @Test
    void temp2() {
        practitionerRepository.deleteById(8l);
    }

}