package com.kidsability.automation.context.secret;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class MailBoxCredentials {
    @Value("${mail_box_credentials.user_name}")
    private String userName;
    @Value("${mail_box_credentials.password}")
    private String password;
}
