package com.chatbot.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import com.chatbot.dto.Messages;
import com.chatbot.service.LLMService;
//import
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.chatbot.dto.Messages.*;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;


public class LLMActor extends AbstractBehavior<Messages.LLMACommand> {

    private final LLMService llmService;

    public LLMActor(ActorContext<LLMACommand> context, LLMService llmService) {
        super(context);
        this.llmService = llmService;
    }

    @Override
    public Receive<LLMACommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(GenerateSearchQuery.class, this::generateSearchQuery)
                .onMessage(GenerateFinalOutput.class, this::generateFinalOutput)
                .build();
    }

    /**
     * Behavuor to generate SEARCH QUERY based on the user question and chat history
     * @param genSearchQuery
     * @return
     */
    private Behavior<Messages.LLMACommand> generateSearchQuery(GenerateSearchQuery genSearchQuery) {

        // Initializing messages array
        List<Message> promptMessages = new ArrayList<>();

        // PRompting a system message to generate a search query
        promptMessages.add(new SystemMessage("""
            You are a helpful assistant tasked with generating a standalone search query based on the user's current question, using prior conversation history for clarification if necessary.
                
                     Instructions:
                     - If the user's current question clearly mentions a topic (e.g., "File Storage"), directly use that topic to create a precise search query.
                     - DO NOT treat the user question as asking for a list of options unless the question explicitly requests options.
                     - If the current question is vague (e.g., "How about this one?"), use the previous conversation history to infer what "this one" refers to and rewrite the query accordingly.
                     - If the user question is already clear, REPHRASE it into a specific, standalone search query.
                     - OUTPUT ONLY the search query — no explanations, no formatting.
                     - Maintain the intent of the user's original question.
                
                     Examples:
                     User: "Tell me about File Storage" → Search Query: "Explain File Storage in Laravel"
                     User: "What about concurrency?" (after discussing Laravel topics) → Search Query: "Explain concurrency features in Laravel"
                
                     Very Important:
                     - You must recognize when a topic is already provided.
                     - Do not ask questions back to the user.
                     - Your output must be a direct, precise search query.
        """));

        // Adding the chat history to the prompt for context
        promptMessages.addAll(genSearchQuery.chatHistory());

        // Sending the prompt to LLM to get a response
        getContext().getLog().info("\n prompt length for generating search query: '{}'", promptMessages.toString().length());
        String llmResponse = llmService.genLLMResponseUsingProps(promptMessages);

        // Sending the LLM Response to Input actor as rewritten query
        genSearchQuery.replyTo().tell(new RewrittenQuery(llmResponse));

        return this;
    }

    /**
     * Behaviour to generate final output response using the context chunks
     * @param genFinalOutput
     * @return
     */
    private Behavior<Messages.LLMACommand> generateFinalOutput(GenerateFinalOutput genFinalOutput) {

        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(new SystemMessage("""
You are an intelligent, helpful assistant specialized in providing reliable, context-aware answers.

Instructions:
- Use ONLY the provided context to answer the user's current question whenever possible.
- Maintain the flow of the previous conversation when appropriate (greetings, confirmations, tone).
- Structure your answers clearly:
  - Use **paragraphs** for explanations.
  - Use **bullet points** for lists or multiple steps.
  - Use **headings** if answering multi-part questions.
- Be clear, concise, and helpful.
- Avoid enclosing the full response inside triple backticks (` ``` `) unless explicitly necessary.

Handling Missing Context:
- If the context contains sufficient information, answer strictly based on the context.
- If the context does NOT contain sufficient information to fully answer the user's specific question:
  - Politely inform the user that **the specific information for their query was not found in the provided context**.
  - Then, **provide a helpful answer based on your general knowledge**, outside the context.
  - Clearly state that the following explanation is based on general knowledge, not the given context.

Example phrasing when context is missing:
- "The specific information about your query was not found in the provided context. However, based on general knowledge, here is an explanation:"

Special Handling:
- If the user message is a greeting ("hi", "hello", "hey"), respond warmly and politely without technical content.
- If the user's question is unclear, ask for clarification politely without assuming.

Reminder:
- Prioritize answering based on the provided context.
- If context is insufficient, help the user openly and transparently.
- Maintain a natural, honest, and user-friendly tone.
                """));

        // Adding chat history to prompt messages
        promptMessages.addAll(genFinalOutput.chatHistory());

        // adding user question to prompt messages
        promptMessages.add(new UserMessage("""
                ***CONTEXT***
                %s
                
                ### User's Current Question:
                %s
                """.formatted(String.join("\n", genFinalOutput.contextChunks()), genFinalOutput.question())));


        getContext().getLog().info("\n prompt length of final output: '{}'", promptMessages.toString().length());

        String llmResponse = llmService.genLLMResponseUsingProps(promptMessages);

        genFinalOutput.replyTo().tell(new FinalAnswer(llmResponse));
        return this;
    }

    public static Behavior<LLMACommand> create(LLMService llmService) {
        return Behaviors.setup(context -> new LLMActor(context, llmService));
    }

}
