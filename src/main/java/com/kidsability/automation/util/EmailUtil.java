package com.kidsability.automation.util;

import com.kidsability.automation.context.secret.MailBoxCredentials;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.util.Properties;

public class EmailUtil {
    public static void sendEmail(String msg, String subject, String address, MailBoxCredentials mailBoxCredentials) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "outlook.office365.com");
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailBoxCredentials.getUserName(), mailBoxCredentials.getPassword());
            }
        });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("admin-capstone@kidsability.ca"));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(address));
        message.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }
}
