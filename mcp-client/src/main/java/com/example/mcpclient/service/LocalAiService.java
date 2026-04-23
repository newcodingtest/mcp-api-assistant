package com.example.mcpclient.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class LocalAiService {
    private final ChatClient chatClient;

    public LocalAiService(ChatClient.Builder chatClientBuilder,
                          ToolCallbackProvider toolCallbackProvider) {

        ToolCallback[] callbacks = toolCallbackProvider.getToolCallbacks();
        System.out.println("tool count = " + callbacks.length);
        for (var callback : callbacks) {
            System.out.println("tool callback = " + callback.getToolDefinition().name());
        }


        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        너는 사내 API 명세를 읽어주는 백엔드 개발 도우미다.
                                   사용자의 질문이 특정 프로젝트의 Swagger/OpenAPI 명세 조회와 관련되면
                                   반드시 제공된 MCP tools를 사용해서 서비스를 찾고, endpoint를 찾고,
                                   필요한 경우 상세 명세를 조회한 뒤 답변해라.
                        """)
                .defaultToolCallbacks(callbacks)
                .build();
    }
    public String askMistral(String question) {
        return chatClient.prompt()
                .options(OllamaChatOptions.builder()
                        .keepAlive("10m")
                        .build())
                .user(question)
                .call()
                .content();
    }
}
