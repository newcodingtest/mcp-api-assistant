package com.example.mcpserver.tool;

import com.example.mcpserver.model.*;
import com.example.mcpserver.service.OpenApiDocumentService;
import com.example.mcpserver.service.OpenApiRegistryService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class SwaggerTools {

    private final OpenApiRegistryService registryService;
    private final OpenApiDocumentService documentService;

    public SwaggerTools(OpenApiRegistryService registryService,
                        OpenApiDocumentService documentService) {
        this.registryService = registryService;
        this.documentService = documentService;
    }

    @McpTool(name = "get_api_usage_guide", description = "특정 API의 명세 기반 사용 가이드를 반환한다.")
    public ApiUsageGuide getApiUsageGuide(
            @McpToolParam(description = "서비스 이름", required = true) String serviceName,
            @McpToolParam(description = "API path", required = true) String path,
            @McpToolParam(description = "HTTP method", required = true) String method
    ) {
        OperationSpec spec = documentService.getOperationSpec(serviceName, path, method);
        if (spec == null) {
            throw new IllegalArgumentException("Operation spec not found. serviceName=" + serviceName
                    + ", path=" + path + ", method=" + method);
        }

        List<ParamGuide> pathParameters = extractParametersByIn(spec, "path");
        List<ParamGuide> queryParameters = extractParametersByIn(spec, "query");
        List<FieldGuide> requestBodyFields = extractRequestBodyFields(spec);
        Map<String, Object> requestExample = buildRequestExample(requestBodyFields);
        String responseType = extractResponseType(spec);
        List<String> responseStatusCodes = extractResponseStatusCodes(spec);
        String security = extractSecurity(spec);
        String usageTip = buildUsageTip(spec, pathParameters, queryParameters, requestBodyFields);

        return new ApiUsageGuide(
                safe(spec.serviceName()),
                safe(spec.path()),
                safe(spec.method()),
                safe(spec.summary()),
                safe(spec.description()),
                pathParameters,
                queryParameters,
                requestBodyFields,
                requestExample,
                responseType,
                responseStatusCodes,
                security,
                usageTip
        );
    }

    @McpTool(
            name = "find_services",
            description = "프로젝트명 또는 도메인 키워드로 API 서비스를 검색한다. 예: order, auth, user, payment"
    )
    public List<OpenApiServiceInfo> findServices(
            @McpToolParam(description = "검색할 서비스명 또는 도메인 키워드") String keyword
    ) {
        System.out.println("[MCP TOOL] find_services called. keyword="+ keyword);
        String q = keyword == null ? "" : keyword.toLowerCase();

        return registryService.getServices().stream()
                .filter(it ->
                        it.name().toLowerCase().contains(q) ||
                                (it.description() != null && it.description().toLowerCase().contains(q)))
                .sorted(Comparator.comparing(OpenApiServiceInfo::name))
                .toList();
    }

    @McpTool(
            name = "find_endpoints",
            description = "특정 서비스에서 기능 키워드로 endpoint를 검색한다. 예: 주문 생성, 로그인, 환불, 회원가입"
    )
    public List<EndpointSummary> findEndpoints(
            @McpToolParam(description = "서비스 이름. 예: order") String serviceName,
            @McpToolParam(description = "기능 키워드. 예: 주문 생성, login, refund") String keyword,
            @McpToolParam(description = "HTTP Method. 비워도 됨. 예: GET, POST") String method
    ) {
        System.out.println("[MCP TOOL] find_endpoints called. serviceName={}, keyword={}, method={}"+
                serviceName+ " , "+keyword+" , "+method);
        return documentService.findEndpoints(serviceName, keyword, method);
    }

    @McpTool(
            name = "get_operation_spec",
            description = "특정 path와 method의 OpenAPI 상세 명세를 가져온다. requestBody, parameters, responses, security를 포함한다."
    )
    public OperationSpec getOperationSpec(
            @McpToolParam(description = "서비스 이름. 예: order") String serviceName,
            @McpToolParam(description = "API path. 예: /api/v1/orders") String path,
            @McpToolParam(description = "HTTP method. 예: POST") String method
    ) {
        System.out.println("[MCP TOOL] get_operation_spec called. serviceName={}, path={}, method={}" +
                serviceName+ " , "+path+ " , "+method);
        return documentService.getOperationSpec(serviceName, path, method);
    }

    @McpTool(
            name = "summarize_operation",
            description = "특정 API 명세를 백엔드 개발자가 바로 이해할 수 있도록 요약한다."
    )
    public Map<String, Object> summarizeOperation(
            @McpToolParam(description = "서비스 이름") String serviceName,
            @McpToolParam(description = "API path") String path,
            @McpToolParam(description = "HTTP method") String method
    ) {
        System.out.println("[MCP TOOL] summarize_operation called. serviceName={}, path={}, method={}"+
                serviceName+ " , "+path+ " , "+method);
        OperationSpec spec = documentService.getOperationSpec(serviceName, path, method);

        return Map.of(
                "serviceName", spec.serviceName(),
                "path", spec.path(),
                "method", spec.method(),
                "operationId", spec.operationId(),
                "summary", spec.summary(),
                "description", spec.description(),
                "parameterCount", spec.parameters() == null ? 0 : spec.parameters().size(),
                "hasRequestBody", spec.requestBody() != null && !spec.requestBody().isEmpty(),
                "responseStatusCodes", spec.responses() == null ? List.of() : spec.responses().keySet().stream().sorted().toList(),
                "hasSecurity", spec.security() != null && !spec.security().isEmpty()
        );
    }

    private List<ParamGuide> extractParametersByIn(OperationSpec spec, String targetIn) {
        if (spec.parameters() == null || spec.parameters().isEmpty()) {
            return List.of();
        }

        return spec.parameters().stream()
                .filter(p -> targetIn.equalsIgnoreCase(safe(getString(p, "in"))))
                .map(p -> new ParamGuide(
                        safe(getString(p, "name")),
                        normalizeType(getString(p, "type")),
                        safe(getString(p, "description")),
                        getBoolean(p, "required")
                ))
                .toList();
    }

    private List<FieldGuide> extractRequestBodyFields(OperationSpec spec) {
        Object requestBody = spec.requestBody();
        if (requestBody == null) {
            return List.of();
        }

        Object propertiesObj = getNested(requestBody, "properties");
        if (!(propertiesObj instanceof Map<?, ?> properties) || properties.isEmpty()) {
            return List.of();
        }

        List<String> requiredFields = extractRequiredFieldNames(requestBody);

        return properties.entrySet().stream()
                .map(entry -> {
                    String fieldName = String.valueOf(entry.getKey());
                    Object schema = entry.getValue();

                    return new FieldGuide(
                            fieldName,
                            normalizeType(getString(schema, "type")),
                            safe(getString(schema, "description")),
                            safe(getString(schema, "example")),
                            requiredFields.contains(fieldName)
                    );
                })
                .toList();
    }

    private List<String> extractRequiredFieldNames(Object requestBody) {
        Object requiredObj = getNested(requestBody, "required");
        if (requiredObj instanceof List<?> requiredList) {
            return requiredList.stream()
                    .map(String::valueOf)
                    .toList();
        }
        return List.of();
    }

    private Map<String, Object> buildRequestExample(List<FieldGuide> fields) {
        if (fields == null || fields.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> example = new java.util.LinkedHashMap<>();
        for (FieldGuide field : fields) {
            example.put(field.name(), exampleValue(field));
        }
        return example;
    }

    private Object exampleValue(FieldGuide field) {
        if (field.example() != null && !field.example().isBlank()) {
            String type = safe(field.type()).toLowerCase();
            String example = field.example();

            if ("integer".equals(type) || "int".equals(type)) {
                try {
                    return Integer.parseInt(example);
                } catch (Exception ignored) {
                    return example;
                }
            }

            if ("number".equals(type) || "double".equals(type) || "float".equals(type) || "long".equals(type)) {
                try {
                    return Double.parseDouble(example);
                } catch (Exception ignored) {
                    return example;
                }
            }

            if ("boolean".equals(type)) {
                return Boolean.parseBoolean(example);
            }

            return example;
        }

        String type = safe(field.type()).toLowerCase();
        return switch (type) {
            case "integer", "int" -> 0;
            case "number", "double", "float", "long" -> 0;
            case "boolean" -> false;
            case "array" -> List.of();
            case "object" -> Map.of();
            default -> "string";
        };
    }

    private String extractResponseType(OperationSpec spec) {
        if (spec.responses() == null || spec.responses().isEmpty()) {
            return "명세에 정의되지 않음";
        }

        for (Object response : spec.responses().values()) {
            String schemaRef = getString(getNested(response, "schema"), "$ref");
            if (!schemaRef.isBlank()) {
                return extractSimpleRefName(schemaRef);
            }

            String type = getString(getNested(response, "schema"), "type");
            if (!type.isBlank()) {
                return normalizeType(type);
            }
        }

        return "명세에 정의되지 않음";
    }

    private List<String> extractResponseStatusCodes(OperationSpec spec) {
        if (spec.responses() == null || spec.responses().isEmpty()) {
            return List.of("명세에 정의되지 않음");
        }

        return spec.responses().keySet().stream()
                .map(String::valueOf)
                .sorted()
                .toList();
    }

    private String extractSecurity(OperationSpec spec) {
        if (spec.security() == null || spec.security().isEmpty()) {
            return "명세에 정의되지 않음";
        }
        return "정의됨";
    }

    private String buildUsageTip(OperationSpec spec,
                                 List<ParamGuide> pathParameters,
                                 List<ParamGuide> queryParameters,
                                 List<FieldGuide> requestBodyFields) {

        StringBuilder sb = new StringBuilder();

        if ("GET".equalsIgnoreCase(safe(spec.method()))) {
            sb.append("조회 시 호출하는 API이다. ");
        } else if ("POST".equalsIgnoreCase(safe(spec.method()))) {
            sb.append("생성 시 호출하는 API이다. ");
        } else if ("PUT".equalsIgnoreCase(safe(spec.method())) || "PATCH".equalsIgnoreCase(safe(spec.method()))) {
            sb.append("수정 시 호출하는 API이다. ");
        } else if ("DELETE".equalsIgnoreCase(safe(spec.method()))) {
            sb.append("삭제 또는 취소 시 호출하는 API이다. ");
        }

        if (!pathParameters.isEmpty()) {
            sb.append("Path parameter를 URL에 넣어 호출해야 한다. ");
        }

        if (!queryParameters.isEmpty()) {
            sb.append("필요한 query parameter를 함께 전달해야 한다. ");
        }

        if (!requestBodyFields.isEmpty()) {
            sb.append("Request body에 필요한 필드를 JSON 형태로 전달해야 한다.");
        } else {
            sb.append("Request body 없이 호출 가능하다.");
        }

        return sb.toString().trim();
    }

    private String getString(Object source, String fieldName) {
        Object value = getNested(source, fieldName);
        return value == null ? "" : String.valueOf(value);
    }

    private boolean getBoolean(Object source, String fieldName) {
        Object value = getNested(source, fieldName);
        if (value instanceof Boolean b) {
            return b;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    private String extractSimpleRefName(String ref) {
        if (ref == null || ref.isBlank()) {
            return "명세에 정의되지 않음";
        }

        int idx = ref.lastIndexOf('/');
        return idx >= 0 ? ref.substring(idx + 1) : ref;
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return "string";
        }

        return switch (type.toLowerCase()) {
            case "int32", "int64" -> "integer";
            case "float", "double" -> "number";
            default -> type;
        };
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "명세에 정의되지 않음" : value;
    }

    private Object getNested(Object source, String fieldName) {
        if (source == null) {
            return null;
        }

        if (source instanceof Map<?, ?> map) {
            return map.get(fieldName);
        }

        try {
            java.lang.reflect.Method method = source.getClass().getMethod(fieldName);
            return method.invoke(source);
        } catch (Exception ignored) {
        }

        try {
            String getter = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            java.lang.reflect.Method method = source.getClass().getMethod(getter);
            return method.invoke(source);
        } catch (Exception ignored) {
        }

        return null;
    }
}