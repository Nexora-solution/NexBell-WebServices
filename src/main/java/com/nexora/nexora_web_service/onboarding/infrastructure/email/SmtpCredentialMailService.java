package com.nexora.nexora_web_service.onboarding.infrastructure.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.nexora_web_service.onboarding.domain.services.CredentialMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Sends credential emails via the SendGrid HTTP API (https, port 443).
 *
 * We use the HTTP API instead of SMTP on purpose: Render's free tier blocks all
 * outbound SMTP traffic (ports 25/465/587), so JavaMailSender times out there.
 * The HTTP API works on both Render and local.
 *
 * Disabled by default (nexbell.mail.enabled=false): in that mode it just logs
 * the message so the flow works end-to-end without a key. Set
 * nexbell.mail.enabled=true plus sendgrid.api-key to send for real.
 */
@Service
public class SmtpCredentialMailService implements CredentialMailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpCredentialMailService.class);
    private static final String SENDGRID_URL = "https://api.sendgrid.com/v3/mail/send";

    private final boolean enabled;
    private final String from;
    private final String apiKey;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public SmtpCredentialMailService(@Value("${nexbell.mail.enabled:false}") boolean enabled,
                                     @Value("${nexbell.mail.from:no-reply@nexbell.app}") String from,
                                     @Value("${sendgrid.api-key:}") String apiKey) {
        this.enabled = enabled;
        this.from = from;
        this.apiKey = apiKey;
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

        deliver(toPersonalEmail, subject, body);
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

        deliver(toPersonalEmail, subject, body);
    }

    private void deliver(String to, String subject, String body) {
        if (!enabled || apiKey == null || apiKey.isBlank()) {
            log.info("[mail disabled] Credenciales para {}\nAsunto: {}\n{}", to, subject, body);
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "personalizations", List.of(Map.of("to", List.of(Map.of("email", to)))),
                    "from", Map.of("email", from, "name", "NexBell Security"),
                    "subject", subject,
                    "content", List.of(Map.of("type", "text/plain", "value", body))
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SENDGRID_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Credenciales enviadas a {} (SendGrid {})", to, response.statusCode());
            } else {
                log.error("SendGrid rechazó el envío a {}: {} {}", to, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Error enviando credenciales a {} por SendGrid: {}", to, e.getMessage(), e);
        }
    }
}
