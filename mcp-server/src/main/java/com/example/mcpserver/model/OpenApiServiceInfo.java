package com.example.mcpserver.model;

public record OpenApiServiceInfo(
        String name,
        String description,
        String openapiUrl
) {
}