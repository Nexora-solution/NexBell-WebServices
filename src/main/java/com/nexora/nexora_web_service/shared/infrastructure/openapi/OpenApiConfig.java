package com.nexora.nexora_web_service.shared.infrastructure.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nexBellOpenAPI() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("NexBell API")
                        .description("NexBell smart-intercom backend — IAM, directory, onboarding, intercom, security and audit bounded contexts.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components().addSecuritySchemes(schemeName,
                        new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                // Tag order controls the display order in Swagger UI
                // IAM
                .addTagsItem(new Tag().name("Authentication").description("Login, register and token refresh"))
                .addTagsItem(new Tag().name("Sessions").description("Session management (logout)"))
                .addTagsItem(new Tag().name("Passwords").description("Password reset and change"))
                .addTagsItem(new Tag().name("Users").description("Current user profile and FCM token"))
                // Onboarding
                .addTagsItem(new Tag().name("Onboarding").description("Contract creation and resident credential claim"))
                // Directory
                .addTagsItem(new Tag().name("Buildings").description("Buildings registered in the system"))
                .addTagsItem(new Tag().name("Apartments").description("Apartments per building"))
                .addTagsItem(new Tag().name("Residents").description("Resident profiles"))
                .addTagsItem(new Tag().name("Doormen").description("Doorman profiles"))
                .addTagsItem(new Tag().name("Directory Queries").description("Cross-directory search endpoints"))
                // Intercom
                .addTagsItem(new Tag().name("Intercom Queue").description("Visitor queue and SSE stream for doorman dashboard"))
                .addTagsItem(new Tag().name("IoT Ingress (Intercom)").description("Endpoints called by the edge/ESP32 (doorbell, presence)"))
                .addTagsItem(new Tag().name("Resident Decisions").description("Resident approve/deny visitor from mobile"))
                .addTagsItem(new Tag().name("Pre-registered Visits").description("Pre-register expected visitors"))
                .addTagsItem(new Tag().name("Notifications").description("Push notification delivery"))
                // Security
                .addTagsItem(new Tag().name("Door Control").description("Remote door open/close (doorman only)"))
                .addTagsItem(new Tag().name("IoT Ingress").description("Sensor events from edge (motion, door state, face)"))
                .addTagsItem(new Tag().name("Security Alarms").description("Alarm state and SSE stream"))
                .addTagsItem(new Tag().name("Face Recognition").description("Face events and stream"))
                .addTagsItem(new Tag().name("IoT Devices").description("Device provisioning config"))
                .addTagsItem(new Tag().name("Authorization").description("Access authorization checks"))
                // Audit
                .addTagsItem(new Tag().name("Audit Queries").description("Access records and hardware logs"))
                .addTagsItem(new Tag().name("Access Records Audit").description("Audit event ingestion"));
    }
}
