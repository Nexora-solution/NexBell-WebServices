package com.nexora.nexora_web_service.intercom.domain.services;

import java.io.InputStream;

public interface EvidenceStorage {
    String save(String filename, InputStream data);
}
