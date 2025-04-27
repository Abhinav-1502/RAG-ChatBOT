package com.chatbot.service;

import akka.actor.typed.javadsl.ActorContext;
import com.chatbot.actors.LLMActor;
import com.chatbot.dto.ChatHistory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.StreamingChatModel;

import java.util.ArrayList;
import java.util.List;

public class LLMService {


    private final ChatClient chatClient;

    private static final int MAX_CHUNK_TOKENS = 3000;

    public LLMService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

//    public String getAnswer(String question, List<String> contextChunks, ActorContext<LLMActor.LLMCommand> context) {
//        StringBuilder prompt = new StringBuilder();
//
//        // 🎯 System Prompt
////        prompt.append("You are a helpful AI assistant. Your goal is to answer questions based ONLY on the following context.\n");
////        prompt.append("If the answer is not stated directly, reason it out using the closest related information from the context.\n");
////        prompt.append("If it's completely out of context, reply: 'The question is out of context.'\n\n");
//        prompt.append("### DOCUMENT ###\n");
//        String combinedContext = String.join(" ", contextChunks);
//        prompt.append(combinedContext);
//
//        context.getLog().info("🧠 Raw context length: {}", combinedContext.length());
//        context.getLog().info("📦 Total chunks used (unfiltered): {}", contextChunks.size());
//
//        // ❓ Add the actual user question
//        prompt.append("\n\n### QUESTION ###\n").append(question).append("\n");
//
//
//        prompt.append("You're an AI assistant to help the user understand something using the context provided in the document," +
//                "Answer the users QUESTION using the DOCUMENT text above.\n" +
//                "Keep your answer ground in the facts of the DOCUMENT.\n" +
//                "If the DOCUMENT doesn’t contain the facts to answer the QUESTION return 'Question is out of the context'\n");
//
//        // 🤖 Ask LLM for final output
//        return chatClient
//                .prompt()
//                .user(prompt.toString())
//                .call()
//                .content();
//    }

    public String genLLMResponse(String promptQuerry) {

        // Generates LLM Response from prebuilt querry
        return chatClient
                .prompt()
                .messages()
                .user(promptQuerry)
                .call()
                .content();
    }

    public String genLLMResponseUsingProps(List<Message> messages) {
//        List<Message> messages = new ArrayList<>();
//        messages.add(new SystemMessage(systemMessage));
//        messages.addAll(messagesChatHistory);
//        messages.add(new UserMessage(promptQuerry));

        return chatClient
                .prompt()
                .messages(messages)
                .call()
                .content();
    }

}