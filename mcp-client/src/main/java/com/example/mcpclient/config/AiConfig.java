package com.example.mcpclient.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(
            OllamaChatModel chatModel,
            SyncMcpToolCallbackProvider toolCallbackProvider
    ) {
        System.out.println("===== MCP TOOLS =====");
        Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .forEach(tool -> System.out.println(tool.getToolDefinition()));

        return ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .build();
    }
}