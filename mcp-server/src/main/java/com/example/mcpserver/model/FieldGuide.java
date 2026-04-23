package com.example.mcpserver.model;

public record FieldGuide(
        String name,
        String type,
        String description,
        String example,
        boolean required
) {}