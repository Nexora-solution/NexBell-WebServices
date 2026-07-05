package com.nexora.nexora_web_service.security.interfaces.rest.transform;

import com.nexora.nexora_web_service.iam.domain.services.TokenIssuer;
import com.nexora.nexora_web_service.security.domain.model.commands.DispatchDoorCommand;
import com.nexora.nexora_web_service.security.domain.model.entities.DoorCommand;
import com.nexora.nexora_web_service.security.domain.model.entities.IoTDevice;
import com.nexora.nexora_web_service.security.domain.model.valueobjects.CommandType;
import com.nexora.nexora_web_service.security.domain.services.SecurityCommandService;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.DoorCommandRepository;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.IoTDeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.context.annotation.Import;
import com.nexora.nexora_web_service.iam.infrastructure.security.config.WebSecurityConfig;
import com.nexora.nexora_web_service.iam.infrastructure.security.JwtAuthenticationFilter;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DoorControlController.class)
@Import({WebSecurityConfig.class, JwtAuthenticationFilter.class})
class DoorControlControllerIntegrationTest {

    @Autowired MockMvc mockMvc;

    @MockBean SecurityCommandService securityCommandService;
    @MockBean DoorCommandRepository doorCommandRepository;
    @MockBean IoTDeviceRepository deviceRepository;
    @MockBean TokenIssuer tokenIssuer;

    private static final String TOKEN = "mock-doorman-token";

    @BeforeEach
    void setUp() {
        // Setup authentication mock behavior for JwtAuthenticationFilter
        when(tokenIssuer.validate(TOKEN)).thenReturn(true);
        when(tokenIssuer.getEmailFromToken(TOKEN)).thenReturn("doorman@nexbell.app");
        when(tokenIssuer.getRoleFromToken(TOKEN)).thenReturn("DOORMAN");
    }

    @Test
    void getPhysicalState_whenAuthenticatedAsDoorman_returnsOk() throws Exception {
        var device = new IoTDevice("DEV-ESP32-DOOR01");
        device.updateDoorState("OPEN");
        when(deviceRepository.findByDeviceCode("DEV-ESP32-DOOR01")).thenReturn(Optional.of(device));

        mockMvc.perform(get("/api/security/door/physical-state")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("OPEN"))
                .andExpect(jsonPath("$.online").value(true));
    }

    @Test
    void unlock_whenAuthenticatedAsDoorman_dispatchesUnlockCommandAndReturnsOk() throws Exception {
        var cmd = new DoorCommand(CommandType.UNLOCK);
        when(securityCommandService.handle(any(DispatchDoorCommand.class))).thenReturn(Optional.of(cmd));

        mockMvc.perform(post("/api/security/door/unlock")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commandType").value("UNLOCK"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(securityCommandService).handle(any(DispatchDoorCommand.class));
    }

    @Test
    void unlock_whenUnauthenticated_returnsForbidden() throws Exception {
        // Performing request without Auth header or with invalid token
        mockMvc.perform(post("/api/security/door/unlock")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void unlock_whenAuthenticatedAsResident_returnsForbidden() throws Exception {
        String residentToken = "mock-resident-token";
        when(tokenIssuer.validate(residentToken)).thenReturn(true);
        when(tokenIssuer.getEmailFromToken(residentToken)).thenReturn("resident@nexbell.app");
        when(tokenIssuer.getRoleFromToken(residentToken)).thenReturn("RESIDENT");

        mockMvc.perform(post("/api/security/door/unlock")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
