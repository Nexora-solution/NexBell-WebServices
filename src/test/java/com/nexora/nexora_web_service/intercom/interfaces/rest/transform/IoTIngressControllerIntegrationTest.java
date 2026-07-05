package com.nexora.nexora_web_service.intercom.interfaces.rest.transform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.nexora_web_service.iam.domain.services.TokenIssuer;
import com.nexora.nexora_web_service.intercom.domain.model.commands.AttachVisitorEvidenceCommand;
import com.nexora.nexora_web_service.intercom.domain.model.commands.CreateVisitRequestCommand;
import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;
import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitorEvidence;
import com.nexora.nexora_web_service.intercom.domain.model.valueobjects.EvidenceUri;
import com.nexora.nexora_web_service.intercom.domain.services.IntercomCommandService;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.AttachEvidenceResource;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.CreateVisitRequestResource;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IoTIngressController.class)
@Import({WebSecurityConfig.class, JwtAuthenticationFilter.class})
class IoTIngressControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean IntercomCommandService commandService;
    @MockBean TokenIssuer tokenIssuer; // needed to satisfy security filter bean dependency

    @Test
    void createVisitRequest_whenValidPayload_returnsCreated() throws Exception {
        var resource = new CreateVisitRequestResource("Juan Perez", 101L);
        var expectedRequest = new VisitRequest("Juan Perez", 101L);

        when(commandService.handle(any(CreateVisitRequestCommand.class))).thenReturn(Optional.of(expectedRequest));

        mockMvc.perform(post("/api/intercom/visit-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visitorName").value("Juan Perez"))
                .andExpect(jsonPath("$.apartmentId").value(101L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(commandService).handle(any(CreateVisitRequestCommand.class));
    }

    @Test
    void attachEvidence_whenValidPayload_returnsOk() throws Exception {
        var resource = new AttachEvidenceResource("http://photo.url", "http://audio.url");
        var expectedRequest = new VisitRequest("Juan Perez", 101L);
        var evidence = new VisitorEvidence(new EvidenceUri("http://photo.url"), new EvidenceUri("http://audio.url"));
        expectedRequest.attachEvidence(evidence);

        when(commandService.handle(any(AttachVisitorEvidenceCommand.class))).thenReturn(Optional.of(expectedRequest));

        mockMvc.perform(post("/api/intercom/visit-requests/1/evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").value("http://photo.url"))
                .andExpect(jsonPath("$.audioUrl").value("http://audio.url"));

        verify(commandService).handle(any(AttachVisitorEvidenceCommand.class));
    }
}
