package com.nexora.nexora_web_service.intercom.interfaces.rest.resources;

public record VisitStreamResource(
        Long visitRequestId,
        String streamUrl,
        String protocol,
        String token
) {}
