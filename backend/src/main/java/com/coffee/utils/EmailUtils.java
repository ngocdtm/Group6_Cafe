package com.coffee.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailUtils {

    @Value("${spring.mail.username}")
    private String senderEmail;  // Lấy email từ configuration


    @Autowired
    private JavaMailSender emailSender;

    public void sendPasswordResetEmail(String to, String temporaryPassword) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject("Password Reset - Cafe Management System");

        String htmlMsg = String.format("""
            <html>
                <body>
                    <h2>Password Reset - Cafe Management System</h2>
                    <p>A password reset was requested for your account.</p>
                    <p>Your temporary password is: <strong>%s</strong></p>
                    <p>Please login using this temporary password and change it immediately.</p>
                    <p><a href="http://localhost:4200/login">Click here to login</a></p>
                    <p>If you didn't request this password reset, please contact support immediately.</p>
                </body>
            </html>
            """, temporaryPassword);

        message.setContent(htmlMsg, "text/html; charset=utf-8");
        emailSender.send(message);
    }

    public void sendMessage(String to, String subject, String text, List<String> list) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);  // Sử dụng email đã config
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        if (list != null && !list.isEmpty()) {
            message.setCc(getCcArray(list));
        }
        emailSender.send(message);
    }

    private String[] getCcArray(List<String> ccList){
        String[] cc = new String[ccList.size()];
        for(int i = 0; i < ccList.size(); i++){
            cc[i] = ccList.get(i);
        }
        return cc;
    }

    public void forgotMail(String to, String subject, String password) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("enter your email address to send mail");
        helper.setTo(to);
        helper.setSubject(subject);
        String htmlMsg = "<p><b>Your Login details for Cafe Management System</b><br><b>Email: </b> " + to + " <br><b>Password: </b> " + password + "<br><a href=\"http://localhost:4200/\">Click here to login</a></p>";
        message.setContent(htmlMsg, "text/html");
        emailSender.send(message);
    }
}