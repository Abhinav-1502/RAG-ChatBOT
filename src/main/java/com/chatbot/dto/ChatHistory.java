package com.chatbot.dto;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatHistory {
    private String sessionId;
    private ArrayList<String> messages;

    public ChatHistory(String sessionId, ArrayList<String> messages) {
        this.sessionId = sessionId;
        this.messages = messages;
    }

    public String getSessionId() {
        return sessionId;
    }
    public ArrayList<String> getMessages() {
        return messages;
    }

    public ChatHistory addExchange(String message) {
        this.messages.add(message);
        return this;
    }

    public Boolean isChatEmpty(){
        return messages.isEmpty();
    }

    public List<Message> getMessageListObject(){
        List<Message> messageList = new ArrayList<>();
        messages.forEach((message) -> {
            if( message.startsWith("User:")){
                messageList.add(new UserMessage(message.split(":")[1]));
            } else if ( message.startsWith("Assisant:")) {
                messageList.add(new AssistantMessage(message.split(":")[1]));
            }
        });

        return messageList;
    }
}
