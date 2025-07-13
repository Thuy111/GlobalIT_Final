package com.bob.smash.service;

import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage; 
import java.io.IOException;
import java.util.List;

import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class ContactMailServiceImple implements ContactMailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendContactMail(
        String name,
        String phone,
        String email,
        String messageText,
        List<MultipartFile> files
    ) throws MessagingException, IOException {

        System.out.println("받는사람 이메일" + senderEmail);
        System.out.println("보내는사람 이메일" + email);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(senderEmail); // 받는 사람 이메일 주소
        helper.setSubject("[SMaSh] " + email + " 님의 문의입니다."); // 이메일 제목
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("phone", phone);
        context.setVariable("email", email);
        context.setVariable("messageText", messageText.replace("\n", "<br/>"));

        String html = templateEngine.process("email/contact", context);
        helper.setText(html, true); // HTML 형식의 메시지
        helper.setFrom(email); // 보내는 사람 이메일 주소

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    helper.addAttachment(file.getOriginalFilename(), file);
                }
            }
        }
        mailSender.send(message);
    }
}