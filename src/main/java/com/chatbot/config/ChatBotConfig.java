// ChatBotConfig.java
package com.chatbot.config;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import com.chatbot.actors.*;
import com.chatbot.dto.Messages;
import com.chatbot.searcher.EmbeddingSearcher;
import com.chatbot.service.EmbeddingService;
import com.chatbot.service.LLMService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ChatBotConfig {

    @Bean
    public OpenAiChatModel openAiChatModel() {
        String apiKey = AppProperties.get("spring.ai.openai.api-key");
        OpenAiApi api = new OpenAiApi(apiKey);
        OpenAiChatOptions options = OpenAiChatOptions.builder().model("gpt-3.5-turbo").build();
        return new OpenAiChatModel(api, options);
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    public LLMService llmService(ChatClient chatClient) {
        return new LLMService(chatClient);
    }

    @Bean
    public EmbeddingSearcher embeddingSearcher() throws IOException {
        return new EmbeddingSearcher("laravel-11"); // example domain
    }

    @Bean
    public ActorSystem<Messages.InputCommand> actorSystem(
            LLMService llmService,
            EmbeddingSearcher searcher,
            EmbeddingService embeddingService
    ) {
        return ActorSystem.create(
                this.createRootBehavior(llmService, searcher, embeddingService),
                "RootActorSystem"
        );
    }

    @Bean
    public static Behavior<Messages.InputCommand> createRootBehavior(
            LLMService llmService,
            EmbeddingSearcher embeddingSearcher,
            EmbeddingService embeddingService
    ) {
        return Behaviors.setup(ctx -> {
            // Spawn actors within this actor system
            ActorRef<Messages.LLMACommand> llmActor = ctx.spawn(LLMActor.create(llmService), "LLMActor");
            ActorRef<SearchActor.Command> searchActor = ctx.spawn(SearchActor.create(embeddingSearcher, llmActor, embeddingService), "SearchActor");
            ActorRef<Messages.QueryOCommand> rewriteQueryActor = ctx.spawn(RewriteQueryActor.create(llmActor), "RewriteQueryActor");

            return InputActor.create(searchActor, rewriteQueryActor);
        });
    }


    public ActorRef<Messages.InputCommand> inputActor(ActorSystem<Messages.InputCommand> system) {
        return system;
    }
}