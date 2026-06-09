package com.loanmanager.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void enviarRecuperacionPassword(String email, String nuevaPassword) {
        if (mailSender == null) {
            log.warn("Mail no configurado. Contraseña para {}: {}", email, nuevaPassword);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("LoanManager — Recuperación de contraseña");
            message.setText("Tu nueva contraseña temporal es: " + nuevaPassword +
                "\n\nCámbiala desde Mi Perfil.");
            mailSender.send(message);
            log.info("Email enviado a: {}", email);
        } catch (Exception e) {
            log.error("Error al enviar email: {}", e.getMessage());
        }
    }
}