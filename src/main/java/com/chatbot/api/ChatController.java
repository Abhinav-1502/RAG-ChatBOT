// --- 3. ChatController ---
package com.chatbot.api;

import akka.actor.typed.ActorRef;
import com.chatbot.dto.Messages;
import com.chatbot.dto.ChatHistory;
import com.chatbot.session.SessionManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ActorRef<Messages.InputCommand> mainInputActor;


    private final Map<String, ChatHistory> sessionChatMap = new ConcurrentHashMap<>();
    private final HttpSession httpSession;

    public ChatController(ActorRef<Messages.InputCommand> inputActor, HttpSession httpSession) {
        this.mainInputActor = inputActor;
        this.httpSession = httpSession;
    }

    @PostMapping("/{sessionId}")
    public CompletableFuture<String> chat(@PathVariable("sessionId") String sessionId, @RequestBody ChatRequest request, HttpSession session) {

        SessionManager.register(session.getId(), session);

        ChatHistory chatHistory = (ChatHistory) session.getAttribute("chatHistory");

        if (chatHistory == null) {
            chatHistory = new ChatHistory(sessionId, new ArrayList<>());
        }


        CompletableFuture<String> futureResponse = new CompletableFuture<>();

        mainInputActor.tell(
                new Messages.UserInput(request.getMessage(),
                        chatHistory,
                        futureResponse,
                        session.getId()));

        return futureResponse;
    }

    @GetMapping("/{sessionId}/messages")
    public List<String> getChatMessages(@PathVariable("sessionId") String sessionId, HttpSession session) {
        ChatHistory chatHistory = (ChatHistory) session.getAttribute("chatHistory");

        if (chatHistory == null) {
            return new ArrayList<>();
        }

        return chatHistory.getMessages();
    }


}