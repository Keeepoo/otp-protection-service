package com.promo.otp.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

public class EmailService {
    private static final Logger logger = LogManager.getLogger(EmailService.class);
    private final String username;
    private final String password;
    private final String fromEmail;
    private final Session session;

    public EmailService() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("email.properties")) {
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Cannot load email.properties", e);
        }
        this.username = props.getProperty("email.username");
        this.password = props.getProperty("email.password");
        this.fromEmail = props.getProperty("email.from");
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", props.getProperty("mail.smtp.host"));
        mailProps.put("mail.smtp.port", props.getProperty("mail.smtp.port"));
        mailProps.put("mail.smtp.auth", props.getProperty("mail.smtp.auth"));
        mailProps.put("mail.smtp.starttls.enable", props.getProperty("mail.smtp.starttls.enable"));
        this.session = Session.getInstance(mailProps, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendCode(String toEmail, String code) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Your OTP Code");
            message.setText("Your verification code is: " + code);
            Transport.send(message);
            logger.info("Email sent to {} with code {}", toEmail, code);
        } catch (MessagingException e) {
            logger.error("Failed to send email", e);
        }
    }
}
