package com.movinfo.messenger;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class DiscordWebhookSender {
    private static final String WEBHOOK_URL = System.getenv("DISCORD_WEBHOOK_URL");

    private DiscordWebhookSender(){}

    public static void sendMessage(String message) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("content", message);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(WEBHOOK_URL, HttpMethod.POST, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Failed to send message. HTTP Status: " + response.getStatusCode());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("No Discord Webhook URL found!\n Check env setting");
        }
    }
}