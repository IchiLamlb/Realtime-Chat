package com.example.realtimechat.auth.application;

import com.example.realtimechat.config.MailProperties;
import com.example.realtimechat.user.domain.User;
import com.example.realtimechat.common.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PasswordResetMailService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetMailService.class);

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final org.springframework.boot.autoconfigure.mail.MailProperties springMailProperties;

    public PasswordResetMailService(
            JavaMailSender mailSender,
            MailProperties mailProperties,
            org.springframework.boot.autoconfigure.mail.MailProperties springMailProperties
    ) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
        this.springMailProperties = springMailProperties;
    }

    public void sendResetLink(User user, String rawToken) {
        String resetUrl = UriComponentsBuilder.fromUriString(mailProperties.frontendBaseUrl())
                .queryParam("resetToken", rawToken)
                .build()
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.from());
        message.setTo(user.getEmail());
        message.setSubject("Reset your Realtime Chat password");
        message.setText("""
                Hello %s,

                We received a request to reset your Realtime Chat password.
                Open this link to set a new password:

                %s

                This link expires in %d minutes. If you did not request this, you can ignore this email.
                """.formatted(user.getDisplayName(), resetUrl, mailProperties.passwordResetTokenTtlMinutes()));
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            String password = springMailProperties.getPassword();
            String normalizedPassword = password == null ? null : password.replaceAll("\\s+", "");
            String senderPassword = mailSender instanceof JavaMailSenderImpl sender ? sender.getPassword() : null;
            log.warn(
                    "Password reset email failed via senderClass={}, host={}, port={}, username={}, from={}, passwordConfigured={}, passwordLength={}, normalizedPasswordLength={}, senderPasswordLength={}",
                    mailSender.getClass().getName(),
                    springMailProperties.getHost(),
                    springMailProperties.getPort(),
                    springMailProperties.getUsername(),
                    mailProperties.from(),
                    password != null && !password.isBlank(),
                    password == null ? 0 : password.length(),
                    normalizedPassword == null ? 0 : normalizedPassword.length(),
                    senderPassword == null ? 0 : senderPassword.length(),
                    exception
            );
            throw new BusinessException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "PASSWORD_RESET_EMAIL_UNAVAILABLE",
                    "Password reset email service is unavailable"
            );
        }
    }
}
