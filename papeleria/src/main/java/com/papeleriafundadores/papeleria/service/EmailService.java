package com.papeleriafundadores.papeleria.service;

public interface EmailService {

    //Envío de texto plano rápido (Como alertas internas de stock)
    void sendSimpleEmail(String to, String subject, String text);

    // Envío de plantillas HTML  (facturas, bienvenida de clientes)
    void sendHtmlEmail(String to, String subject, String htmlContent);
}
