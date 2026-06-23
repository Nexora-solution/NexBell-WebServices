package com.nexora.nexora_web_service.intercom.infrastructure.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class MqttVideoSubscriber implements MqttCallback {

    private static final Logger log = LoggerFactory.getLogger(MqttVideoSubscriber.class);

    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.broker.username:}")
    private String brokerUsername;

    @Value("${mqtt.broker.password:}")
    private String brokerPassword;

    private static final String TOPIC = "nexbell/telemetry/video";
    private static final String CLIENT_ID = "NexBell_Cloud_Backend_" + System.currentTimeMillis();

    private MqttClient mqttClient;
    private volatile byte[] latestFrame = null;

    @PostConstruct
    public void init() {
        try {
            mqttClient = new MqttClient(brokerUrl, CLIENT_ID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            if (brokerUsername != null && !brokerUsername.isBlank()) {
                options.setUserName(brokerUsername);
                options.setPassword(brokerPassword.toCharArray());
            }

            mqttClient.setCallback(this);
            mqttClient.connect(options);
            mqttClient.subscribe(TOPIC);
            log.info("Connected to MQTT Broker {} and subscribed to {}", brokerUrl, TOPIC);
        } catch (MqttException e) {
            log.error("Failed to connect to MQTT broker", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            log.error("Error disconnecting MQTT client", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT Connection lost", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        if (TOPIC.equals(topic)) {
            // Store the raw binary JPEG frame
            latestFrame = message.getPayload();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not used for subscriber
    }

    /**
     * Gets the latest JPEG frame received from MQTT.
     */
    public byte[] getLatestFrame() {
        return latestFrame;
    }
}
