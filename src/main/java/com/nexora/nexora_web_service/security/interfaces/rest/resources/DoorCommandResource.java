package com.nexora.nexora_web_service.security.interfaces.rest.resources;

import com.nexora.nexora_web_service.security.domain.model.valueobjects.CommandType;
import java.time.LocalDateTime;

public record DoorCommandResource(
    Long id,
    CommandType commandType,
    String status,
    LocalDateTime dispatchedAt,
    LocalDateTime confirmedAt
) {}
