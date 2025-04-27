//package com.chatbot;
//
//import akka.actor.typed.*;
//import akka.actor.typed.javadsl.*;
//import com.chatbot.actors.*;
//import com.chatbot.config.AppProperties;
//import com.chatbot.dto.ChatHistory;
//import com.chatbot.dto.Messages;
//import com.chatbot.searcher.EmbeddingSearcher;
//import com.chatbot.service.EmbeddingService;
//import com.chatbot.service.LLMService;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.openai.OpenAiChatModel;
//import org.springframework.ai.openai.OpenAiChatOptions;
//import org.springframework.ai.openai.api.OpenAiApi;
//
//import java.util.ArrayList;
//
//
//public class Main {
//
//    //  LLM Service Initiator
//    private static LLMService initLLMService() {
//        // Gets key from the application properties and starts LLM service
//        String apiKey = AppProperties.get("spring.ai.openai.api-key");
//        OpenAiApi openAiApi = new OpenAiApi(apiKey);
//        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
//                .model("gpt-3.5-turbo") // or "gpt-3.5-turbo", "gpt-4-0125-preview"
//                .build();
//
//        OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi, chatOptions);
//        ChatClient chatClient = ChatClient.create(chatModel);
//
//        System.out.println("Chat model information"+chatModel.toString());
//        LLMService llmService = new LLMService(chatClient);
//        return llmService;
//    }
//
//    // Creates a replyToActor to print the final answer
//    public static Behavior<String> createReplyPrinter(){
//        return Behaviors.receive(String.class)
//                .onMessage(String.class, msg->{
//                    System.out.println("\n Final Answer:\n" + msg);
//                    return Behaviors.same();
//                })
//                .build();
//    }
//
//    // Creates a root actor system with llmService object and searcher object
//    public static Behavior<Messages.InputCommand> createRootActorSystem(LLMService llmService, EmbeddingSearcher embeddingSearcher, EmbeddingService  embeddingService) {
//        return Behaviors.setup(ctx->{
//                    ActorRef<LLMActor.LLMCommand> llmActor = ctx.spawn(LLMActor.create(llmService), "LLMActor");
//                    ActorRef<SearchActor.Command> searchActor = ctx.spawn(SearchActor.create(embeddingSearcher, llmActor, embeddingService), "SearchActor");
//                    ActorRef<Messages.QueryOCommand> queryOActor = ctx.spawn(RewriteQueryActor.create(llmActor), "QueryOActor");
//                    ActorRef<String> replyToActor = ctx.spawn(createReplyPrinter(), "ReplyToActor");
//                    ActorRef<Messages.InputCommand> inputActor = ctx.spawn(InputActor.create(searchActor, queryOActor), "InputActor");
//
//
//                    ArrayList<String> chat = new ArrayList<String>();
//                    chat.add("User: Did kratos kill ares?");
//                    chat.add("Bot: Yes, Kratos did kill Ares. After Ares had critically wounded him and reminded him of their past interactions, Kratos coldly replied and stabbed Ares through the heart with the Blade of the Gods, ultimately killing him. This led to Kratos doing what was thought impossible, a mere mortal slaying a God, and saving Athens from destruction.");
//                    chat.add("User: Why?");
//                    chat.add("Bot: Kratos sought revenge against Ares because he was enraged and disgusted by the actions Ares made him commit, including killing innocent people and his own family. Ares believed that by making Kratos kill his family, he was shaping him into a great warrior. Despite Ares' attempt to justify his actions, Kratos ultimately saw through his manipulation and chose to avenge the deaths of his loved ones by killing Ares.");
//                    ChatHistory chatHistory = new ChatHistory("1", chat);
//                    inputActor.tell(new Messages.UserInput("why did ares do that?", chatHistory));
//
//                    return InputActor.create(searchActor, queryOActor);
//                });
//    }
//
//    public static void main(String[] args) throws Exception {
//        // Initiating LLM service with OpenAI key creds
////        LLMService llmService = initLLMService();
////        // setting the index searcher
////        String apiKey = AppProperties.get("spring.ai.openai.api-key");
////
////        // We should pass which embedded chunks to use for searching
////        EmbeddingSearcher embeddingSearcher = new EmbeddingSearcher("kratos");
////        EmbeddingService embeddingService = new EmbeddingService(apiKey); // Use your key from AppProperties
////        // Create the actor system with root guardian
////        ActorSystem<Messages.InputCommand> system = ActorSystem.create(createRootActorSystem(llmService, embeddingSearcher, embeddingService), "RootActorSystem");
//
//    }
//}
