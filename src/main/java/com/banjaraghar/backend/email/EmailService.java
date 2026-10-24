package com.banjaraghar.backend.email;

import com.banjaraghar.backend.auth.enums.OtpPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String username) {
        this.mailSender = mailSender;
        this.fromAddress = "Banjara Ghar <" + username + ">";
    }

    @Async
    public void sendOtpEmail(String to, String otp, OtpPurpose purpose, long validMinutes) {
        String subject = purpose == OtpPurpose.SIGNUP
                ? "Verify your email - Banjara Ghar"
                : "Reset your password - Banjara Ghar";

        String body = "Your Banjara Ghar verification code is: " + otp + "\n\n"
                + "This code expires in " + validMinutes + " minutes. "
                + "If you did not request it, you can safely ignore this email.";

        send(to, subject, body);
    }

    @Async
    public void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException e) {
            // Nobody is waiting for this call anymore, so the failure can only be logged
            log.error("Failed to send email '{}' to {}: {}", subject, to, e.getMessage());
        }
    }
}
