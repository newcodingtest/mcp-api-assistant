package com.example.mcpserver.dto;

import java.util.List;

public class ServiceApiListResponse {

    private final String keyword;
    private final int count;
    private final List<ServiceSummary> services;

    public ServiceApiListResponse(String keyword, int count, List<ServiceSummary> services) {
        this.keyword = keyword;
        this.count = count;
        this.services = services;
    }

    public String getKeyword() {
        return keyword;
    }

    public int getCount() {
        return count;
    }

    public List<ServiceSummary> getServices() {
        return services;
    }

    public static class ServiceSummary {
        private final String name;
        private final String description;
        private final String openApiUrl;

        public ServiceSummary(String name, String description, String openApiUrl) {
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
}