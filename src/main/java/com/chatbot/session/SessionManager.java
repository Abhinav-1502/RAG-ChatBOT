package com.chatbot.session;

import jakarta.servlet.http.HttpSession;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class SessionManager {
    private static final Map<String, HttpSession> sessionMap = new ConcurrentHashMap<>();

    public static void register(String sessionId, HttpSession session) {
        sessionMap.put(sessionId, session);
    }

    public static HttpSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }
}