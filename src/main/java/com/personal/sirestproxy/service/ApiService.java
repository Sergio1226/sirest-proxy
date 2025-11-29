package com.personal.sirestproxy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${api.url}")
    private String urlLogin;

    private String authToken = null;
    private Instant tokenExpiry = Instant.EPOCH;
    @Value("${api.user}")
    private String user;        
    @Value("${api.password}")
    private String password; 

    public String login() {
        if (user == null || password == null) {
            throw new RuntimeException("Usuario o contraseña no configurados");
        }

        Map<String, String> body = new HashMap<>();
        body.put("user", user);
        body.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(urlLogin, request, Map.class);

        
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Login fallido: " + response.getStatusCode());
        }

        authToken = (String) response.getBody().get("validateToken");
        tokenExpiry = Instant.now().plus(6, ChronoUnit.HOURS);

        return authToken;
    }

    private void ensureValidToken() {
        if (authToken == null || Instant.now().isAfter(tokenExpiry)) {
            login();
        }
    }
    
    public Map<String, Object> sirest(int month, int day, int type) {
        ensureValidToken();
        
        int year = Instant.now().atZone(ZoneId.of("America/Bogota")).getYear();
        String mm = String.format("%02d", month);
        String dd = String.format("%02d", day);
        
        String url = String.format(
                "https://servicios2.uptc.edu.co/SiRestauranteBackEnd/Menus/menusFechaRestaurante/1/%d/%d-%s-%s",
                type, year, mm, dd
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        headers.set("Authentication", authToken);
        headers.set("Program", "LchYI/jKSgcZTdYZ3VppnZkBx3fTVchhQg6AhruDu1HLXrEv/6NjJCNjlY2jIpUwg1M8ipkosHsNovSQZjaDJg==");
        headers.set("UpdateToken", "TsEpyeRh6s1WveQc/2AnPUNVj8KAHu3CilgoZgjxYJeAN187kS2ZysusIOJYjLW8QpCN+bD9lnoPSMKRLguezOeRskCAg4rHBgxdpEsvhSk=");
        headers.set("User", user);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error HTTP: " + response.getStatusCode());
            }
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
