package com.example.Ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;

    public AiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String askAI(String prompt) {

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}