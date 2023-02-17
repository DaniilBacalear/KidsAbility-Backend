package com.kidsability.automation.service;

import com.kidsability.automation.record.Credentials;
import com.kidsability.automation.record.SessionToken;
import com.kidsability.automation.secret.MailBoxCredentials;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class AccountService {
    private MailBoxCredentials mailBoxCredentials;
    public AccountService(MailBoxCredentials mailBoxCredentials) {
        this.mailBoxCredentials = mailBoxCredentials;
    }
    private String generateTempPassword() {
        int length = 20;
        return RandomStringUtils.random(length, true, true);
    }

}
