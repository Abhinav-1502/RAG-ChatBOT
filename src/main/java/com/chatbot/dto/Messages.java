package com.chatbot.dto;

import akka.actor.typed.ActorRef;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Messages {
    public interface InputCommand {}


    public record UserInput(String question, ChatHistory chatHistory,  CompletableFuture<String> replyTo, String httpSessionId) implements InputCommand{}
    public record RewrittenQuery(String query) implements InputCommand {}
    public record FinalAnswer(String answer) implements InputCommand {}


    public interface QueryOCommand {}

    public record QueryData(String question, ChatHistory chatHistory, ActorRef<InputCommand> inputActorRef) implements QueryOCommand {}

    public interface LLMACommand {}

    public record GenerateSearchQuery(String question, List<Message> chatHistory, ActorRef replyTo) implements LLMACommand {}
    public record GenerateFinalOutput(String question, List<Message> chatHistory, List<String> contextChunks, ActorRef replyTo) implements LLMACommand {}

}
