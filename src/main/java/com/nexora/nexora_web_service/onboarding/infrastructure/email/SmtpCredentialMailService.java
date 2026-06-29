package com.nexora.nexora_web_service.onboarding.infrastructure.email;

import com.nexora.nexora_web_service.onboarding.domain.services.CredentialMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends credential emails via SMTP. Disabled by default (nexbell.mail.enabled=false):
 * in that mode it just logs the message so the flow works end-to-end without SMTP
 * configured. Set nexbell.mail.enabled=true plus spring.mail.* to send for real.
 */
@Service
public class SmtpCredentialMailService implements CredentialMailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpCredentialMailService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public SmtpCredentialMailService(JavaMailSender mailSender,
                                     @Value("${nexbell.mail.enabled:false}") boolean enabled,
                                     @Value("${nexbell.mail.from:no-reply@nexbell.app}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    @Override
    public void sendDoormanCredentials(String toPersonalEmail, String loginEmail, String password, String buildingName) {
        String subject = "Tus credenciales NexBell — " + buildingName;
        String body = "Hola,\n\n"
                + "Estas son tus credenciales para iniciar sesión en NexBell (" + buildingName + "):\n\n"
                + "  Usuario:     " + loginEmail + "\n"
                + "  Contraseña:  " + password + "\n\n"
                + "Por seguridad, cambia tu contraseña en el primer ingreso.\n\n"
                + "— Equipo NexBell";

        if (!enabled) {
            log.info("[mail disabled] Credenciales para {}\nAsunto: {}\n{}", toPersonalEmail, subject, body);
            return;
        }

        send(toPersonalEmail, subject, body);
    }

    @Override
    public void sendResidentCredentials(String toPersonalEmail, String loginEmail, String password, String buildingName, String apartmentCode) {
        String where = buildingName + (apartmentCode == null || apartmentCode.isBlank() ? "" : " — Depto. " + apartmentCode);
        String subject = "Tus credenciales NexBell — " + where;
        String body = "Hola,\n\n"
                + "Estas son tus credenciales para iniciar sesión en NexBell (" + where + "):\n\n"
                + "  Usuario:     " + loginEmail + "\n"
                + "  Contraseña:  " + password + "\n\n"
                + "Al iniciar sesión por primera vez en la app, registra tu nombre y teléfono\n"
                + "para activar tu departamento. Por seguridad, cambia tu contraseña.\n\n"
                + "— Equipo NexBell";

        if (!enabled) {
            log.info("[mail disabled] Credenciales para {}\nAsunto: {}\n{}", toPersonalEmail, subject, body);
            return;
        }

        send(toPersonalEmail, subject, body);
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Credenciales enviadas a {}", to);
    }
}
