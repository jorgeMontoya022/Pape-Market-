package com.papeleriafundadores.papeleria.service.impl;

import com.papeleriafundadores.papeleria.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private static final String SENDER_NAME = "Papelería Fundadores <no-reply@papeleriafundadores.com>";

    @Async
    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(SENDER_NAME);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Correo de texto enviado con éxito a: {}", to);
        } catch (MailException e) {
            // Método @Async: la excepción no llega al llamador porque se ejecuta
            // en un hilo separado. Solo registramos con la causa completa para diagnóstico.
            // No relanzamos — nadie puede capturarlo desde un contexto asíncrono.
            log.error("No se pudo enviar el correo simple a {}", to, e);
        }
    }

    @Async
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(SENDER_NAME);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("Correo HTML institucional enviado con éxito a: {}", to);
        } catch (MailException | MessagingException e) {
            // Mismo criterio que sendSimpleEmail: captura específica, log con causa completa,
            // sin relanzar desde un método @Async.
            log.error("No se pudo enviar el correo HTML a {}", to, e);
        }
    }
}
