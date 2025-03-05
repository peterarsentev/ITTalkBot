package ru.job4j.it.talk.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.User;
import ru.job4j.it.talk.service.ui.TgButtons;

import java.util.HashMap;
import java.util.function.Function;

@Service
@AllArgsConstructor
@Slf4j
public class StatisticService {
    private TgButtons tgButtons;

    public void process(User user, ContentSender receive) {
    }

    public String flagEmoji(String lang) {
        var languageFlags = new HashMap<String, String>();
        languageFlags.put("en", "🇺🇸");
        languageFlags.put("es", "🇪🇸");
        languageFlags.put("fr", "🇫🇷");
        languageFlags.put("de", "🇩🇪");
        languageFlags.put("it", "🇮🇹");
        languageFlags.put("pt", "🇵🇹");
        languageFlags.put("nl", "🇳🇱");
        languageFlags.put("ru", "🇷🇺");
        languageFlags.put("ja", "🇯🇵");
        languageFlags.put("zh", "🇨🇳");
        return languageFlags.get(lang);
    }

    private String timeLabel(int total) {
        int hours = total / 3600; // Calculate hours
        int minutes = (total % 3600) / 60; // Calculate remaining minutes
        int seconds = total % 60; // Calculate remaining seconds
        StringBuilder timeLabel = new StringBuilder();
        if (hours > 0) {
            timeLabel.append(hours).append(" ч. ").append(" ");
        }
        if (minutes > 0) {
            timeLabel.append(minutes).append(" мин.").append(" ");
        }
        timeLabel.append(seconds).append(" сек.");
        return timeLabel.toString();
    }

    private String determineVocabularyLevel(int vocabularySize) {
        if (vocabularySize < 100) {
            return "A0 (0 - 100)";
        }
        if (vocabularySize <= 300) {
            return "A1 (100 - 300)";
        }
        if (vocabularySize <= 600) {
            return "A2 (300 - 600)";
        }
        if (vocabularySize <= 1200) {
            return "B1 (600 - 1200)";
        }
        if (vocabularySize <= 2500) {
            return "B2 (1200 - 2500)";
        }
        if (vocabularySize <= 5000) {
            return "C1 (2500 - 5000)";
        }
        return "C2 (5000 > ...)";
    }
}
