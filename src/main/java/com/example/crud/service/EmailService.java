package com.example.crud.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;


@Service
public class EmailService implements EmailServ {


    private JavaMailSender mailSender;
    private SpringTemplateEngine templateEngine;
    private MessageSource messageSource;

    @Autowired
    public EmailService( JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendVerificationEmail(String to,String firstName, String subject, String verificationCode) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // email recipient, subject, and sender
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom("SmartSteer@outlook.com");

        // for thymeleaf template
        Context context = new Context();
        context.setVariable("verificationCode", verificationCode);
        context.setVariable("firstName", firstName);
        // Render email template with thymeleaf
        String htmlContent = templateEngine.process("email", context);
        helper.setText(htmlContent, true); // 'true' enables HTML content

        mailSender.send(message);
    }

    @Override
    public void passwordChangedEmail(String to, String firstName, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // email recipient, subject, and sender
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom("SmartSteer@outlook.com");

        // for thymeleaf template
        Context context = new Context();
        context.setVariable("firstName", firstName);
        // Render email template with thymeleaf
        String htmlContent = templateEngine.process("pass_email", context);
        helper.setText(htmlContent, true); // 'true' enables HTML content

        mailSender.send(message);
    }

    @Override
    public void passwordForgottenEmail(String to,String firstName, String subject, String resetToken) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // email recipient, subject, and sender
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom("SmartSteer@outlook.com");

        // for thymeleaf template
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("resetToken", resetToken);
        // Render email template with thymeleaf
        String htmlContent = templateEngine.process("passForgotten_email", context);
        helper.setText(htmlContent, true); // 'true' enables HTML content

        mailSender.send(message);
    }
    @Override
    public void accountDeletedEmail(String to, String firstName, Locale locale, Date updatedAt) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Fetch localized messages
        String subject = messageSource.getMessage("account.deleted.subject", null, locale);
        String body = messageSource.getMessage("account.deleted.body", new Object[]{updatedAt}, locale);

        // Set email recipient, subject, and sender
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom("SmartSteer@outlook.com");

        // Set up Thymeleaf context
        Context context = new Context(locale);
        context.setVariable("firstName", firstName);
        context.setVariable("body", body);

        // Render email template with Thymeleaf
        String htmlContent = templateEngine.process("deleted_email", context);
        helper.setText(htmlContent, true); // 'true' enables HTML content

        mailSender.send(message);
    }
}

