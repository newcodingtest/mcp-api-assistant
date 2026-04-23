package com.example.mcpserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "swagger.registry")
public class SwaggerRegistryProperties {
    private List<ServiceItem> services = new ArrayList<>();

    public List<ServiceItem> getServices() {
        return services;
    }

    public void setServices(List<ServiceItem> services) {
        this.services = services;
    }

    public static class ServiceItem {
        private String name;
        private String description;
        private String openapiUrl;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getOpenapiUrl() {
            return openapiUrl;
        }

        public void setOpenapiUrl(String openapiUrl) {
            this.openapiUrl = openapiUrl;
        }
    }
}
