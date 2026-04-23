package com.example.mcpserver.model;

import java.util.List;

public record EndpointSummary(
        String serviceName,
        String path,
        String method,
        String operationId,
        String summary,
        String description,
        List<String> tags
) {
}