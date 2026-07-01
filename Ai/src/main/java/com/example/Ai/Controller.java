package com.example.Ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping
public class Controller {

    private final AiService aiService;

    public Controller(AiService aiService) {
        this.aiService = aiService;
    }



    @GetMapping("/")
    public String helloWorld(){
        return "hello world";
    }

    @GetMapping("/recommendations/{username}")
    public List<RecommendedCard> getRecommendations(@PathVariable String username) throws Exception {
        return aiService.generateRecommendationsForUser(username);
    }

    @GetMapping("/statements/{username}")
    public List<?> getStatementsByUsername(@PathVariable String username){
        return aiService.getStatementsByUserName(username);
    }
}

