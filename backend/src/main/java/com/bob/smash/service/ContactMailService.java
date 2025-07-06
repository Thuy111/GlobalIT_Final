package com.bob.smash.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import jakarta.mail.MessagingException;
import java.io.IOException;

public interface ContactMailService {
    void sendContactMail(
        String name,
        String phone,
        String email,
        String messageText,
        List<MultipartFile> files
    ) throws MessagingException, IOException;
}