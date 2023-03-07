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


}