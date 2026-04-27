package com.example.mcpclient.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

@Service
public class LocalAiService {

    private final ChatClient chatClient;

    public LocalAiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String ask(String userMessage) {
        return chatClient.prompt()
                .system("""
                        너는 OpenAPI 문서 분석 전용 assistant다.
                                   
                          반드시 MCP tool 실행 결과만 근거로 답변한다.
                          tool 호출 JSON 자체를 사용자에게 출력하지 않는다.
                          tool 결과에 없는 path, method, schema는 절대 만들지 않는다.
                          예시 API를 임의로 만들지 않는다.
                        """)
                .user(userMessage)
                .call()
                .content();
    }
}
