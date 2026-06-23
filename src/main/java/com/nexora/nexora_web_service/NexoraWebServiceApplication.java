package com.nexora.nexora_web_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class    NexoraWebServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexoraWebServiceApplication.class, args);
    }
}
