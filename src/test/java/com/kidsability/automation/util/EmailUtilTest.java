package com.kidsability.automation.util;

import com.kidsability.automation.context.secret.MailBoxCredentials;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailUtilTest {
    @Autowired
    private MailBoxCredentials mailBoxCredentials;

    @Test
    void sendEmail() {
        var address = "dbacalea@uwaterloo.ca";
        var msg = "this is a test";
        var subject = "test";
        try {
            EmailUtil.sendEmail(msg, subject, address, mailBoxCredentials);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}