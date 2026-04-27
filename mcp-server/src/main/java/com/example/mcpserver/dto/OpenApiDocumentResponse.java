package com.example.mcpserver.dto;

public class OpenApiDocumentResponse {

    private final String serviceName;
    private final String openApiUrl;
    private final String document;

    public OpenApiDocumentResponse(String serviceName, String openApiUrl, String document) {
        this.serviceName = serviceName == null ? "" : serviceName;
        this.openApiUrl = openApiUrl == null ? "" : openApiUrl;
        this.document = document == null ? "ERROR: document is null" : document;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getOpenApiUrl() {
        return openApiUrl;
    }

    public String getDocument() {
        return document;
    }
}
