package com.example.mcpserver.service;

import com.example.mcpserver.model.EndpointSummary;
import com.example.mcpserver.model.OpenApiServiceInfo;
import com.example.mcpserver.model.OperationSpec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OpenApiDocumentService {

    private final OpenApiRegistryService registryService;
    private final RestClient restClient;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    private final Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();

    public OpenApiDocumentService(OpenApiRegistryService registryService) {
        this.registryService = registryService;
        this.restClient = RestClient.builder().build();
    }

    public List<EndpointSummary> findEndpoints(String serviceName, String keyword, String method) {
        Map<String, Object> doc = load(serviceName);
        Map<String, Object> paths = castMap(doc.get("paths"));

        List<EndpointSummary> results = new ArrayList<>();
        if (paths == null) {
            return results;
        }

        for (Map.Entry<String, Object> pathEntry : paths.entrySet()) {
            String path = pathEntry.getKey();
            Map<String, Object> operations = castMap(pathEntry.getValue());
            if (operations == null) {
                continue;
            }

            for (Map.Entry<String, Object> opEntry : operations.entrySet()) {
                String httpMethod = opEntry.getKey().toUpperCase();
                if (method != null && !method.isBlank() && !httpMethod.equalsIgnoreCase(method)) {
                    continue;
                }

                Map<String, Object> operation = castMap(opEntry.getValue());
                if (operation == null) {
                    continue;
                }

                String summary = asString(operation.get("summary"));
                String description = asString(operation.get("description"));
                String operationId = asString(operation.get("operationId"));
                List<String> tags = castList(operation.get("tags"));

                String haystack = String.join(" ",
                        nullSafe(path),
                        nullSafe(summary),
                        nullSafe(description),
                        nullSafe(operationId),
                        String.join(" ", tags == null ? Collections.emptyList() : tags)
                ).toLowerCase();

                if (keyword == null || keyword.isBlank() || haystack.contains(keyword.toLowerCase())) {
                    results.add(new EndpointSummary(
                            serviceName,
                            path,
                            httpMethod,
                            operationId,
                            summary,
                            description,
                            tags == null ? List.of() : tags
                    ));
                }
            }
        }

        return results;
    }

    public OperationSpec getOperationSpec(String serviceName, String path, String method) {
        Map<String, Object> doc = load(serviceName);
        Map<String, Object> paths = castMap(doc.get("paths"));
        if (paths == null || !paths.containsKey(path)) {
            throw new IllegalArgumentException("Path not found: " + path);
        }

        Map<String, Object> pathItem = castMap(paths.get(path));
        Map<String, Object> operation = castMap(pathItem.get(method.toLowerCase()));
        if (operation == null) {
            throw new IllegalArgumentException("Method not found: " + method + " " + path);
        }

        return new OperationSpec(
                serviceName,
                path,
                method.toUpperCase(),
                asString(operation.get("operationId")),
                asString(operation.get("summary")),
                asString(operation.get("description")),
                castMapList(operation.get("parameters")),
                castMap(operation.get("requestBody")),
                castMap(operation.get("responses")),
                castMapList(operation.get("security"))
        );
    }

    public Map<String, Object> load(String serviceName) {
        return cache.computeIfAbsent(serviceName.toLowerCase(), key -> {
            OpenApiServiceInfo serviceInfo = registryService.getService(serviceName);

            String body = restClient.get()
                    .uri(serviceInfo.openapiUrl())
                    .accept(MediaType.APPLICATION_JSON, MediaType.valueOf("application/yaml"), MediaType.TEXT_PLAIN)
                    .retrieve()
                    .body(String.class);

            if (body == null || body.isBlank()) {
                throw new IllegalStateException("Empty OpenAPI document for service: " + serviceName);
            }

            try {
                return jsonMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            } catch (Exception ignored) {
                try {
                    return yamlMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to parse OpenAPI document for service: " + serviceName, e);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private List<String> castList(Object value) {
        return (List<String>) value;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castMapList(Object value) {
        return value == null ? List.of() : (List<Map<String, Object>>) value;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}