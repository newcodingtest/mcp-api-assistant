package com.example.mcpserver.service;

import com.example.mcpserver.config.SwaggerRegistryProperties;
import com.example.mcpserver.model.OpenApiServiceInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenApiRegistryService {

    private final SwaggerRegistryProperties properties;

    public OpenApiRegistryService(SwaggerRegistryProperties properties) {
        this.properties = properties;
    }

    public List<OpenApiServiceInfo> getServices() {
        return properties.getServices().stream()
                .map(it -> new OpenApiServiceInfo(
                        it.getName(),
                        it.getDescription(),
                        it.getOpenapiUrl()
                ))
                .toList();
    }

    public OpenApiServiceInfo getService(String name) {
        return getServices().stream()
                .filter(it -> it.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown service: " + name));
    }
}