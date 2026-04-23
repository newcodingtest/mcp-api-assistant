package com.example.mcpserver.model;

import java.util.List;
import java.util.Map;

public record OperationSpec(
        String serviceName,
        String path,
        String method,
        String operationId,
        String summary,
        String description,
        List<Map<String, Object>> parameters,
        Map<String, Object> requestBody,
        Map<String, Object> responses,
        List<Map<String, Object>> security
) {
}