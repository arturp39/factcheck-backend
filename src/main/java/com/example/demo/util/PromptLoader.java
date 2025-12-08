package com.example.demo.util;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class PromptLoader {

    public String loadPrompt() {
        try (InputStream is = getClass().getResourceAsStream("/prompt.txt")) {
            if (is == null) {
                throw new RuntimeException("Prompt file not found: /prompt.txt");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompt file", e);
        }
    }
}