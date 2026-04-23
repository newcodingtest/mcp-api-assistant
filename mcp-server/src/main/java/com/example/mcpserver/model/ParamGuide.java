package com.example.mcpserver.model;

public record ParamGuide(
        String name,
        String type,
        String description,
        boolean required
) {}
