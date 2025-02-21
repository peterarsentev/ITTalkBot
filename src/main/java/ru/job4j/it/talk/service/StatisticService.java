package ru.job4j.it.talk.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.User;
import ru.job4j.it.talk.model.UserStatistic;
import ru.job4j.it.talk.repository.UserStatisticRepository;
import ru.job4j.it.talk.service.ui.TgButtons;

import java.util.HashMap;
import java.util.function.Function;

@Service
@AllArgsConstructor
@Slf4j
public class StatisticService {
    private UserStatisticRepository userStatisticRepository;
    private TgButtons tgButtons;

    public void process(User user, Function<Content, Integer> receive) {
        var allStatistics = userStatisticRepository.findAllByOrderByVocabularySizeDesc();
        var message = new StringBuilder("📚 *Статистика словаря*:\n\n");
        int topCount = Math.min(30, allStatistics.size());
        for (int i = 0; i < topCount; i++) {
            UserStatistic stat = allStatistics.get(i);
            var flagEmoji = flagEmoji(stat.getLang());
            String rankEmoji = "";
            if (i == 0) {
                rankEmoji = "🥇"; // Gold
            } else if (i == 1) {
                rankEmoji = "🥈"; // Silver
            } else if (i == 2) {
                rankEmoji = "🥉"; // Bronze
            }
            if (stat.getUser().equals(user)) {
                message.append(String.format("%d. %s *%d* сл. %s *%s*\n  %s, %s\n",
                        i + 1, rankEmoji, stat.getVocabularySize(), flagEmoji,
                        stat.getUser().getName().replaceAll("_", " "),
                        determineVocabularyLevel(stat.getVocabularySize()), timeLabel(stat.getSpentTime())));
            } else {
                message.append(String.format("%d. %s *%d* сл. %s %s\n   %s, %s\n",
                        i + 1, rankEmoji, stat.getVocabularySize(), flagEmoji,
                        stat.getUser().getName().replaceAll("_", " "),
                        determineVocabularyLevel(stat.getVocabularySize()), timeLabel(stat.getSpentTime())));
            }
            message.append("\n");
        }

        // Find user's rank
        int userRank = -1;
        for (int i = 0; i < allStatistics.size(); i++) {
            if (allStatistics.get(i).getUser().equals(user)) {
                userRank = i;
                break;
            }
        }

        // If user is ranked higher than 5, show nearby ranks
        if (userRank >= 5) {
            message.append("\n...\n");
            int start = Math.max(0, userRank - 3);
            for (int i = start; i <= userRank; i++) {
                UserStatistic stat = allStatistics.get(i);
                var flagEmoji = flagEmoji(stat.getLang());
                String rankEmoji = "";

                // Add emoji for first three positions
                if (i == 0) {
                    rankEmoji = "🥇"; // Gold
                } else if (i == 1) {
                    rankEmoji = "🥈"; // Silver
                } else if (i == 2) {
                    rankEmoji = "🥉"; // Bronze
                }

                // Formatting message based on user rank
                if (stat.getUser().equals(user)) {
                    message.append(String.format("%d. %s *%d* слов %s *%s*\n\n*%s*, *%s*\n",
                            i + 1, rankEmoji, stat.getVocabularySize(), flagEmoji,
                            stat.getUser().getName().replaceAll("_", " "),
                            determineVocabularyLevel(stat.getVocabularySize()), timeLabel(stat.getSpentTime())));
                } else {
                    message.append(String.format("%d. %s *%d* слов %s *%s*\n\n*%s*, *%s*\n",
                            i + 1, rankEmoji, stat.getVocabularySize(), flagEmoji,
                            stat.getUser().getName().replaceAll("_", " "),
                            determineVocabularyLevel(stat.getVocabularySize()), timeLabel(stat.getSpentTime())));
                }
            }
        }

        message.append(String.format("\n📊 Всего пользователей: %s", allStatistics.size()));
        receive.apply(Content.of()
                .chatId(user.getChatId())
                .text(message.toString())
                .buttons(tgButtons.hide())
                .build()
        );
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
