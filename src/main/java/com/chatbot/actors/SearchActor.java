package com.chatbot.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import com.chatbot.dto.ChatHistory;
import com.chatbot.dto.Messages;
import com.chatbot.searcher.EmbeddingSearcher;
import com.chatbot.searcher.LuceneSearcher;
import com.chatbot.service.EmbeddingService;
import com.chatbot.service.QdrantHelper;

import java.util.List;


public class SearchActor extends AbstractBehavior<SearchActor.Command>{
    public interface Command {}

    public static class Search implements Command {
        public final String optimisedQuery;
        public final String userQuestion;
        public final ActorRef replyTo;
        public ChatHistory chatHistory;

        public Search(String optimisedQuery, String userQuestion, ActorRef replyTo, ChatHistory chatHistory) {
            this.optimisedQuery = optimisedQuery;
            this.userQuestion = userQuestion;
            this.replyTo = replyTo;
            this.chatHistory = chatHistory;
        }
    }

    public static Behavior<Command> create(EmbeddingSearcher searcher, ActorRef<Messages.LLMACommand> llmActor, EmbeddingService embeddingService) {
        return Behaviors.setup(context -> new SearchActor(context, searcher, embeddingService, llmActor));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Search.class, this::processSearch)
                .build();
    }

    private final EmbeddingSearcher searcher;
    private final EmbeddingService embeddingService;
    private final ActorRef<Messages.LLMACommand> llmActor;

    private SearchActor(ActorContext<Command> context, EmbeddingSearcher searcher, EmbeddingService embeddingService, ActorRef<Messages.LLMACommand> llmActor) {
        super(context);
        this.searcher = searcher;
        this.embeddingService = embeddingService;
        this.llmActor = llmActor;
    }

    private Behavior<Command> processSearch(Search msg) {

        getContext().getLog().info("\n🔍 Generating embedding for: {}", msg.optimisedQuery);
        try {

//              Generate embeddings for the user query and search in qdrant DB
            List<Double> queryEmbedding = embeddingService.getEmbeddingVector(msg.optimisedQuery);
            String serverResponse = searcher.searchInQdrantDB(queryEmbedding, 3, "laravel_embeddings");

            getContext().getLog().info("\nGot response from Qdrant DB: {} ", serverResponse);

//              getContext().getLog().info("📚 Found {} semantically matched chunks", topChunks.size());

            List<String> topChunks = QdrantHelper.extractTextsFromQdrantResponse(serverResponse);
            llmActor.tell(new Messages.GenerateFinalOutput(msg.userQuestion, msg.chatHistory.getMessageListObject(), topChunks, msg.replyTo));
//            llmActor.tell(new LLMActor.GenerateAnswer(msg.optimisedQuery, topChunks, msg.replyTo, "finalResultQuery"));

        } catch (Exception e) {
            getContext().getLog().error("❌ Embedding search failed", e);
            msg.replyTo.tell("Search failed due to internal error.");
        }

        return this;
    }

}
