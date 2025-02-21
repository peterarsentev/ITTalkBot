package ru.job4j.it.talk.service.util;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LevelLangPrompt {
    private final Map<String, String> levels = new HashMap<>();

    {
        levels.put("A1", "You speak like a friendly person with simple words. Keep it short and easy.");
        levels.put("A2", "You speak simply and clearly with easy words. Keep sentences short.");
        levels.put("B1", "You speak friendly with simple, clear sentences and some bigger words.");
        levels.put("B2", "You speak casually with advanced words, but it's still easy to follow.");
        levels.put("C1", "You use advanced words naturally, with idioms, but it feels like a normal conversation.");
    }

    public String prompt(String key) {
        return levels.get(key);
    }
}
