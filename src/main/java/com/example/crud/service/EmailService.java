package com.example.crud.service;


import com.example.crud.dto.LocationDTO;
import com.example.crud.entity.EmergencyContact;
import com.example.crud.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Date;
import java.util.List;


@Service
public class EmailService implements EmailServ {


    private JavaMailSender mailSender;
    private SpringTemplateEngine templateEngine;
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
    public void ownerEmail(String to, String firstName,String lastName, String subject) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // email recipient, subject, and sender
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom("SmartSteer@outlook.com");

        // for thymeleaf template
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        // Render email template with thymeleaf
        String htmlContent = templateEngine.process("owner", context);
        helper.setText(htmlContent, true); // 'true' enables HTML content

        mailSender.send(message);
    }

    @Override
    public void Sendnotify(String to, String firstName, String addedByName, String addedByPhone, String phone) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("You Have Been Added as an Emergency Contact");
        helper.setFrom("SmartSteer@outlook.com");

        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("addedByName", addedByName);
        context.setVariable("addedByPhone", addedByPhone);
        context.setVariable("phone", phone);

        String htmlContent = templateEngine.process("notifyEmergency", context);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    @Override
    public void accountDeletedEmail(String to, String firstName, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        //email recipient, subject, and sender
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom("SmartSteer@outlook.com");

        //for thymeleaf template
        Context context = new Context();
        context.setVariable("firstName", firstName);
        // Render email template with thymeleaf
        String htmlContent = templateEngine.process("deleted_email", context);
        helper.setText(htmlContent, true); // 'true' enables HTML content

        mailSender.send(message);
    }

    public void sendEmergencyEmails(User user, List<EmergencyContact> contacts, String location) {
        for (EmergencyContact contact : contacts) {
            try {
                Context context = new Context();
                context.setVariable("firstName", contact.getName());
                context.setVariable("userName", user.getFirstName() + " " + user.getLastName());
                context.setVariable("locationLink", location);

                String htmlContent = templateEngine.process("emergency.html", context);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setTo(contact.getEmail());
                helper.setSubject("🚨 EMERGENCY ALERT - Smart Steer 🚨");
                helper.setFrom("SmartSteer@outlook.com");
                helper.setText(htmlContent, true);
                mailSender.send(message);

                System.out.println("Email sent to " + contact.getEmail());
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }
}

