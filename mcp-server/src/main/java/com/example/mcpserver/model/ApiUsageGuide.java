package com.example.mcpserver.model;

import java.util.List;
import java.util.Map;

public record ApiUsageGuide(
        String serviceName,
        String path,
        String method,
        String summary,
        String description,
        List<ParamGuide> pathParameters,
        List<ParamGuide> queryParameters,
        List<FieldGuide> requestBodyFields,
        Map<String, Object> requestExample,
        String responseType,
        List<String> responseStatusCodes,
        String security,
        String usageTip
) {
}