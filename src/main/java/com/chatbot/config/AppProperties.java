package com.chatbot.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppProperties {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppProperties.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties not found!");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load properties", ex);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}