package com.organization.taskmanagement.notification.service;

import com.organization.taskmanagement.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Sends plain-text email in the background. Without MAIL_HOST configured, the email is
 * written to the log instead, so notifications and password reset links work in development.
 */
@Slf4j
@Component
public class EmailSender {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String mailHost;
    private final String from;

    public EmailSender(ObjectProvider<JavaMailSender> mailSender,
                       @Value("${spring.mail.host:}") String mailHost,
                       AppProperties properties) {
        this.mailSender = mailSender;
        this.mailHost = mailHost;
        this.from = properties.mail().from();
    }

    @Async
    public void send(String to, String subject, String body) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (mailHost == null || mailHost.isBlank() || sender == null) {
            log.info("Email (MAIL_HOST not set, not sent)\nTo: {}\nSubject: {}\n\n{}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
        } catch (MailException ex) {
            log.warn("Could not send email to {}: {}", to, ex.getMessage());
        }
    }
}
