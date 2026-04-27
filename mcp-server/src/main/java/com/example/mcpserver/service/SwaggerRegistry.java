package com.example.mcpserver.service;

import com.example.mcpserver.model.SwaggerServiceDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SwaggerRegistry {

    private final List<SwaggerServiceDefinition> services = new ArrayList<SwaggerServiceDefinition>();

    public SwaggerRegistry() {
        services.add(new SwaggerServiceDefinition(
                "test1",
                "테스트 서비스1 주문/결제 관련 API",
                "http://localhost:8082/v1/api-docs"
        ));
    }

    public List<SwaggerServiceDefinition> findAll() {
        return new ArrayList<SwaggerServiceDefinition>(services);
    }

    public Optional<SwaggerServiceDefinition> findByExactName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT);

        return services.stream()
                .filter(service -> service.getName().toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst();
    }

    public List<SwaggerServiceDefinition> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        String normalized = keyword.trim().toLowerCase(Locale.ROOT);

        return services.stream()
                .filter(service ->
                        service.getName().toLowerCase(Locale.ROOT).contains(normalized)
                                || service.getDescription().toLowerCase(Locale.ROOT).contains(normalized))
                .collect(Collectors.toList());
    }
}