package com.example.mcpserver.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenApiDocumentService {

    private final RestClient restClient;

    public OpenApiDocumentService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String fetchRawDocument(String url) {
        ResponseEntity<String> response = restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Swagger/OpenAPI 문서 조회 실패: " + response.getStatusCode());
        }

        String body = response.getBody();
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalStateException("Swagger/OpenAPI 문서가 비어있습니다.");
        }

        return body;
    }
}
