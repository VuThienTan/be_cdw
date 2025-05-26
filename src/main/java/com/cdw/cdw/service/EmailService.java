package com.cdw.cdw.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariable("resetUrl", frontendUrl + "/reset-password?token=" + token);
        context.setVariable("name", to.split("@")[0]); // Lấy phần trước @ làm tên

        String emailContent = templateEngine.process("password-reset-template", context);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Đặt lại mật khẩu");
        helper.setText(emailContent, true);

        mailSender.send(message);
    }
}

