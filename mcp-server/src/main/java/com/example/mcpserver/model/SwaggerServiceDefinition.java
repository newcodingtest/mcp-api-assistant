package com.example.mcpserver.model;

public class SwaggerServiceDefinition {

    private final String name;
    private final String description;
    private final String openApiUrl;

    public SwaggerServiceDefinition(String name, String description, String openApiUrl) {
        this.name = name;
        this.description = description;
        this.openApiUrl = openApiUrl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOpenApiUrl() {
        return openApiUrl;
    }
}