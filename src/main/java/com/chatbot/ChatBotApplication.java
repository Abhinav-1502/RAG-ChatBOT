package com.chatbot;

import com.chatbot.api.ChatController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;


@SpringBootApplication
@ImportAutoConfiguration(OpenAiAutoConfiguration.class)
public class ChatBotApplication {
    public static void main(String[] args) {

        SpringApplication.run(ChatBotApplication.class, args);
    }


}