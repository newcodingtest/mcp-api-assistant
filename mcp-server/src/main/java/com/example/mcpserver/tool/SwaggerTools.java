package com.example.mcpserver.tool;

import com.example.mcpserver.model.EndpointSummary;
import com.example.mcpserver.model.OpenApiServiceInfo;
import com.example.mcpserver.model.OperationSpec;
import com.example.mcpserver.service.OpenApiDocumentService;
import com.example.mcpserver.service.OpenApiRegistryService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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
}