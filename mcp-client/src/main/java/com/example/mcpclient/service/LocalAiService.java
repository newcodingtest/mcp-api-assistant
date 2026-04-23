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
                        너는 사내 Swagger/OpenAPI 명세를 읽어주는 백엔드 개발 도우미다.
                                          
                        반드시 MCP tools로 조회한 명세 정보만 사용해서 답변해라.
                        명세에 없는 내용은 절대 추론하지 마라.
                                          
                        규칙:
                        1. path parameter, query parameter, request body를 반드시 구분해서 설명한다.
                        2. request body가 없으면 없다고 명시한다.
                        3. security 정보가 없으면 Authorization 헤더를 임의로 추가하지 않는다.
                        4. response status code는 명세에 있는 값만 설명한다.
                        5. 없는 정보는 '명세에 정의되지 않음'이라고 답한다.
                        6. summarize_operation만으로 답하지 말고, 필요한 경우 get_operation_spec를 사용해서 상세 명세를 확인한다.
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
