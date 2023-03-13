package com.kidsability.automation.service;

import com.kidsability.automation.repository.PractitionerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AccountServiceTest {
    @Autowired
    private AccountService accountService;
    @Autowired
    private PractitionerRepository practitionerRepository;


}