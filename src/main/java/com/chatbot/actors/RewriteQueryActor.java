package com.chatbot.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.chatbot.dto.Messages;
import com.chatbot.dto.Messages.*;

public class RewriteQueryActor extends AbstractBehavior<QueryOCommand> {

    private ActorRef<Messages.LLMACommand> llmActor;
    @Override
    public Receive<QueryOCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(Messages.QueryData.class, this::processQuery)
                .build();
    }

    // Behavior on receiving message
    private Behavior<QueryOCommand> processQuery(Messages.QueryData query) {
        getContext().getLog().info("\n Sending the question: '{}', to generate optimised search query", query.question());

        llmActor.tell(new GenerateSearchQuery(query.question(), query.chatHistory().getMessageListObject(), query.inputActorRef()));
//        llmActor.tell(new LLMActor.GenerateAnswer(query.question(), query.chatHistory().getMessages(), query.inputActorRef(), "searchQuery"));
        return this;
    }

    public RewriteQueryActor(ActorContext<QueryOCommand> context, ActorRef<Messages.LLMACommand> llmActor) {
        super(context);
        this.llmActor = llmActor;
    }

    public static Behavior<QueryOCommand> create(ActorRef<Messages.LLMACommand> llmActor) {
        return Behaviors.setup(ctx -> new RewriteQueryActor(ctx, llmActor));
    }


}
