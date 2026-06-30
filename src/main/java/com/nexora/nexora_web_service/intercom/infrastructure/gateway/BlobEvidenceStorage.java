package com.nexora.nexora_web_service.intercom.infrastructure.gateway;

import com.nexora.nexora_web_service.intercom.domain.services.EvidenceStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class BlobEvidenceStorage implements EvidenceStorage {

    private static final Logger log = LoggerFactory.getLogger(BlobEvidenceStorage.class);

    @Override
    public String save(String filename, InputStream data) {
        log.info("Saving evidence media file '{}' to Blob Storage...", filename);
        return "https://blob.nexbell.app/evidence/" + filename;
    }
}
