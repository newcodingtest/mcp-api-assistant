package com.example.mcpclient.controller;

import com.example.demo.dto.ChatRequest;
import com.example.mcpclient.dto.ChatResponse;
import com.example.mcpclient.service.LocalAiService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final LocalAiService localAiService;

    public AiController(LocalAiService localAiService) {
        this.localAiService = localAiService;
    }

    @PostMapping("/swagger")
    public ChatResponse ask(@RequestBody ChatRequest request) {
        System.out.println("request = " + request);
        String answer = localAiService.ask(request.getMessage());
        System.out.println("answer = " + answer);
        return new ChatResponse(answer);
    }
}