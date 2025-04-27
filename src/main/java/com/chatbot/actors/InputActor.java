package com.chatbot.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import com.chatbot.dto.ChatHistory;
import com.chatbot.dto.Messages.*;
import com.chatbot.session.SessionManager;
import jakarta.servlet.http.HttpSession;

import java.util.concurrent.CompletableFuture;


public class InputActor extends AbstractBehavior<InputCommand> {

    private final ActorRef<SearchActor.Command> searchActor;
    private final ActorRef<QueryOCommand> queryOActor;
    private String userQuestion;
    private CompletableFuture<String> replyFuture;
    private ChatHistory chatHistory;
    private String httpSessionId;

    public InputActor(ActorContext<InputCommand> context, ActorRef<SearchActor.Command> searchActor, ActorRef<QueryOCommand> queryOActor) {
        super(context);
        this.searchActor = searchActor;
        this.queryOActor = queryOActor;
        this.userQuestion = "Sample user question";
    }

    @Override
    public Receive<InputCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(UserInput.class, this::processUserInput)
                .onMessage(RewrittenQuery.class, this::processSearch)
                .onMessage(FinalAnswer.class, this::processFinalAnswer)
                .build();
    }

    private Behavior<InputCommand> processFinalAnswer(FinalAnswer finalAnswer) {
        getContext().getLog().info("Received final answer: {}", finalAnswer.answer());

        // Store LLM Response in chat history
        chatHistory.addExchange("User: " + userQuestion);
        chatHistory.addExchange("Assistant: " + finalAnswer.answer());
        HttpSession httpSession = SessionManager.getSession(httpSessionId);
        httpSession.setAttribute("chatHistory", chatHistory);

        if (replyFuture != null && !replyFuture.isDone()) {
            replyFuture.complete(finalAnswer.answer());
        }
        return Behaviors.same();
    }

    private Behavior<InputCommand> processSearch(RewrittenQuery rewrittenQuery) {

        getContext().getLog().info(" Got the optimised search query: '{}' sending to search Actor:", rewrittenQuery.query());
        searchActor.tell(new SearchActor.Search(rewrittenQuery.query(), this.userQuestion, getContext().getSelf(), this.chatHistory));
        return Behaviors.same();
    }

    private Behavior<InputCommand> processUserInput(UserInput userInput) {
        // Change the state of the actor
        this.replyFuture = userInput.replyTo();
        this.chatHistory = userInput.chatHistory();
        this.httpSessionId = userInput.httpSessionId();
        // Get the user query and chat history and send it to rewriteQueryActor
        this.userQuestion = userInput.question();

        //Logg
        getContext().getLog().info("\nRecieved User Input, Question: '{}', ChatHistorySizeL: {}", userInput.question(), userInput.chatHistory().getMessages().size());

        if (chatHistory.isChatEmpty()){
            searchActor.tell(new SearchActor.Search(userInput.question(), this.userQuestion, getContext().getSelf(), this.chatHistory));
        }else{
            queryOActor.tell(new QueryData(userInput.question(), userInput.chatHistory(), getContext().getSelf()));
        }


        return Behaviors.same();
    }

//    private Behavior<Messages.InputCommand> processInputQuery(Command command) {
//        if (command instanceof UserInput) {
//            UserInput msg = (UserInput) command;
//            getContext().getLog().info("original question: {}", msg.query);
//            msg.searchActor.tell(new SearchActor.Search(msg.query, msg.replyTo));
//        }
//        return this;
//    }

    public static Behavior<InputCommand> create(ActorRef<SearchActor.Command> searchActor, ActorRef<QueryOCommand> queryOActor) {
        return Behaviors.setup(ctx -> new InputActor(ctx, searchActor, queryOActor));
    }
}
