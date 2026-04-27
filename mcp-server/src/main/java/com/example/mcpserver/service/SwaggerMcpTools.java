package com.example.mcpserver.service;

import com.example.mcpserver.dto.ServiceApiListResponse;
import com.example.mcpserver.model.SwaggerServiceDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Map;


@Component
public class SwaggerMcpTools {

    private final SwaggerRegistry swaggerRegistry;
    private final OpenApiDocumentService openApiDocumentService;
    private final ObjectMapper objectMapper;

    public SwaggerMcpTools(SwaggerRegistry swaggerRegistry,
                           OpenApiDocumentService openApiDocumentService,
                           ObjectMapper objectMapper) {
        this.swaggerRegistry = swaggerRegistry;
        this.openApiDocumentService = openApiDocumentService;
        this.objectMapper = objectMapper;
    }


    @McpTool(
            name = "get_openapi_endpoint_summary",
            description = "정확한 서비스명(name)으로 등록된 OpenAPI 문서의 핵심 엔드포인트를 요약한다."
    )
    public String getOpenApiEndpointSummary(
            @McpToolParam(description = "정확한 서비스명(name)", required = true)
            String serviceName
    ) {
        try {
            Optional<SwaggerServiceDefinition> serviceOpt =
                    swaggerRegistry.findByExactName(serviceName);

            if (!serviceOpt.isPresent()) {
                return "ERROR: 등록되지 않은 서비스명입니다: " + serviceName;
            }

            SwaggerServiceDefinition service = serviceOpt.get();
            String document = openApiDocumentService.fetchRawDocument(service.getOpenApiUrl());

            if (document == null || document.trim().isEmpty()) {
                return "ERROR: OpenAPI 문서가 비어 있습니다. serviceName=" + serviceName;
            }

            JsonNode root = objectMapper.readTree(document);

            String title = root.path("info").path("title").asText("");
            String description = root.path("info").path("description").asText("");
            String version = root.path("info").path("version").asText("");

            String serverUrl = "";
            JsonNode servers = root.path("servers");
            if (servers.isArray() && servers.size() > 0) {
                serverUrl = servers.get(0).path("url").asText("");
            }

            JsonNode paths = root.path("paths");
            if (paths.isMissingNode() || paths.isEmpty()) {
                return "ERROR: OpenAPI paths가 비어 있습니다. serviceName=" + serviceName;
            }

            StringBuilder sb = new StringBuilder();

            sb.append("serviceName: ").append(service.getName()).append("\n");
            sb.append("title: ").append(title).append("\n");
            sb.append("description: ").append(description).append("\n");
            sb.append("version: ").append(version).append("\n");
            sb.append("serverUrl: ").append(serverUrl).append("\n\n");

            sb.append("endpoints:\n");

            Iterator<Map.Entry<String, JsonNode>> pathFields = paths.fields();

            while (pathFields.hasNext()) {
                Map.Entry<String, JsonNode> pathEntry = pathFields.next();

                String path = pathEntry.getKey();
                JsonNode pathNode = pathEntry.getValue();

                Iterator<Map.Entry<String, JsonNode>> methodFields = pathNode.fields();

                while (methodFields.hasNext()) {
                    Map.Entry<String, JsonNode> methodEntry = methodFields.next();

                    String method = methodEntry.getKey().toUpperCase();
                    JsonNode operation = methodEntry.getValue();

                    String summary = operation.path("summary").asText("");
                    String operationDescription = operation.path("description").asText("");
                    String operationId = operation.path("operationId").asText("");

                    sb.append("- ").append(method).append(" ").append(path).append("\n");
                    sb.append("  operationId: ").append(operationId).append("\n");
                    sb.append("  summary: ").append(summary).append("\n");
                    sb.append("  description: ").append(operationDescription).append("\n");

                    appendParameters(sb, operation);
                    appendRequestBody(sb, root, operation);
                    appendResponses(sb, root, operation);

                    sb.append("\n");
                }
            }

            return sb.toString();

        } catch (Exception e) {
            return "ERROR: OpenAPI endpoint summary 생성 실패. serviceName="
                    + serviceName + ", reason=" + e.getMessage();
        }
    }

    private void appendParameters(StringBuilder sb, JsonNode operation) {
        JsonNode parameters = operation.path("parameters");

        if (!parameters.isArray() || parameters.size() == 0) {
            return;
        }

        sb.append("  parameters:\n");

        for (JsonNode parameter : parameters) {
            sb.append("    - name: ").append(parameter.path("name").asText("")).append("\n");
            sb.append("      in: ").append(parameter.path("in").asText("")).append("\n");
            sb.append("      required: ").append(parameter.path("required").asBoolean(false)).append("\n");
            sb.append("      description: ").append(parameter.path("description").asText("")).append("\n");
            sb.append("      type: ").append(parameter.path("schema").path("type").asText("")).append("\n");
        }
    }

    private void appendRequestBody(StringBuilder sb, JsonNode root, JsonNode operation) {
        JsonNode requestBody = operation.path("requestBody");

        if (requestBody.isMissingNode() || requestBody.isEmpty()) {
            return;
        }

        sb.append("  requestBody:\n");
        sb.append("    required: ").append(requestBody.path("required").asBoolean(false)).append("\n");
        sb.append("    description: ").append(requestBody.path("description").asText("")).append("\n");

        JsonNode schema = requestBody
                .path("content")
                .path("application/json")
                .path("schema");

        appendSchemaInfo(sb, root, schema, "    ");
    }

    private void appendResponses(StringBuilder sb, JsonNode root, JsonNode operation) {
        JsonNode responses = operation.path("responses");

        if (responses.isMissingNode() || responses.isEmpty()) {
            return;
        }

        sb.append("  responses:\n");

        Iterator<Map.Entry<String, JsonNode>> fields = responses.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> responseEntry = fields.next();

            String statusCode = responseEntry.getKey();
            JsonNode response = responseEntry.getValue();

            sb.append("    - status: ").append(statusCode).append("\n");
            sb.append("      description: ").append(response.path("description").asText("")).append("\n");

            JsonNode schema = response
                    .path("content")
                    .path("application/json")
                    .path("schema");

            appendSchemaInfo(sb, root, schema, "      ");
        }
    }

    private void appendSchemaInfo(StringBuilder sb, JsonNode root, JsonNode schema, String indent) {
        if (schema == null || schema.isMissingNode() || schema.isEmpty()) {
            return;
        }

        String ref = schema.path("$ref").asText("");

        if (!ref.isEmpty()) {
            String schemaName = ref.substring(ref.lastIndexOf("/") + 1);
            JsonNode resolved = root.path("components").path("schemas").path(schemaName);

            sb.append(indent).append("schema: ").append(schemaName).append("\n");

            JsonNode required = resolved.path("required");
            if (required.isArray() && required.size() > 0) {
                sb.append(indent).append("requiredFields: ");

                for (int i = 0; i < required.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(required.get(i).asText());
                }

                sb.append("\n");
            }

            JsonNode properties = resolved.path("properties");
            if (!properties.isMissingNode() && !properties.isEmpty()) {
                sb.append(indent).append("fields:\n");

                Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();

                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> fieldEntry = fields.next();

                    String fieldName = fieldEntry.getKey();
                    JsonNode field = fieldEntry.getValue();

                    sb.append(indent).append("  - ").append(fieldName)
                            .append(": ")
                            .append(field.path("type").asText(""))
                            .append(" / ")
                            .append(field.path("description").asText(""))
                            .append("\n");
                }
            }
        }
    }


    @McpTool(
            name = "get_service_api_list",
            description = "등록된 Swagger/OpenAPI 서비스 목록을 조회한다. 입력값은 서비스명 또는 설명 검색 키워드다."
    )
    public ServiceApiListResponse getServiceApiList(
            @McpToolParam(description = "서비스명 또는 설명 검색 키워드", required = true)
            String keyword
    ) {
        String safeKeyword = keyword == null ? "" : keyword.trim();

        List<SwaggerServiceDefinition> matched = swaggerRegistry.search(safeKeyword);

        List<ServiceApiListResponse.ServiceSummary> summaries = matched.stream()
                .map(service -> new ServiceApiListResponse.ServiceSummary(
                        service.getName(),
                        service.getDescription(),
                        service.getOpenApiUrl()
                ))
                .collect(Collectors.toList());

        return new ServiceApiListResponse(safeKeyword, summaries.size(), summaries);
    }

    @McpTool(
            name = "get_openapi_document",
            description = "정확한 서비스명(name)으로 등록된 Swagger/OpenAPI 문서 원문(JSON 또는 YAML)을 조회한다."
    )
    public String getOpenApiDocument(
            @McpToolParam(description = "정확한 서비스명(name)", required = true)
            String serviceName
    ) {
        Optional<SwaggerServiceDefinition> serviceOpt = swaggerRegistry.findByExactName(serviceName);

        if (!serviceOpt.isPresent()) {
            return "ERROR: 등록되지 않은 서비스명입니다: " + serviceName;
        }

        SwaggerServiceDefinition service = serviceOpt.get();

        try {
            String document = openApiDocumentService.fetchRawDocument(service.getOpenApiUrl());

            if (document == null || document.trim().isEmpty()) {
                return "ERROR: OpenAPI 문서가 비어 있습니다. url=" + service.getOpenApiUrl();
            }

            return document;

        } catch (Exception e) {
            return "ERROR: OpenAPI 문서 조회 실패. url="
                    + service.getOpenApiUrl()
                    + ", reason="
                    + e.getMessage();
        }
    }
}
