package ru.job4j.it.talk.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.User;
import ru.job4j.it.talk.model.Voice;
import ru.job4j.it.talk.repository.UserStatisticRepository;
import ru.job4j.it.talk.repository.UserVocabularyRepository;
import ru.job4j.it.talk.repository.VoiceRepository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.function.Function;

@Service
@Slf4j
@AllArgsConstructor
public class Analyze {

    private final VoiceRepository voiceRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final UserStatisticRepository userStatisticRepository;
    private final DailyAimService dailyAimService;

    public void processVoice(User user, Path originVoice, String text, String lang,
                             Integer messageId, String translateText, Function<Content, Integer> receive) {
        var words = text.split("[^a-zA-Z0-9]+");
        int duration = estimateSpeechTime(words);
        var voice = new Voice();
        voice.setUser(user);
        voice.setText(text);
        voice.setLang(lang);
        voice.setDuration(duration);
        voice.setCreated(LocalDateTime.now());
        voice.setMessageId(messageId);
        voice.setTranslateText(translateText);
        voiceRepository.save(voice);
        dailyAimService.process(voice, receive);
        var currentVocabulariesSize = countVocabularySize(user, lang);
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            userVocabularyRepository.upsertUserVocabulary(
                    user.getId(), word.toLowerCase(), lang, LocalDateTime.now());
        }
        receive.apply(
                Content.of().text(String.format("_Вы получили награду:_ *%d* \uD83D\uDC8E.", words.length))
                        .chatId(user.getChatId())
                        .build()
        );
        int vocabularySize = countVocabularySize(user, lang);  // Calculate unique vocabulary size
        userStatisticRepository.upsertUserStatistic(user.getId(), lang, vocabularySize, duration);
        int updatedVocabularySize = countVocabularySize(user, lang);
        if (updatedVocabularySize > currentVocabulariesSize) {
            receive.apply(
                    Content.of().text(String.format("_Поздравляю, Вы расширили словарный запас:_ +*%d* 📚",
                                    updatedVocabularySize - currentVocabulariesSize))
                            .chatId(user.getChatId())
                            .build()
            );
        }
    }

    public void processText(User user, String text, String lang,
                            Integer messageId, String translateText,
                            Function<Content, Integer> receive) {
        var words = text.split("[^a-zA-Z0-9]+");
        var voice = new Voice();
        voice.setUser(user);
        voice.setText(text);
        voice.setLang(lang);
        voice.setDuration(estimateSpeechTime(words));
        voice.setCreated(LocalDateTime.now());
        voice.setMessageId(messageId);
        voice.setTranslateText(translateText);
        voiceRepository.save(voice);
        var currentVocabulariesSize = countVocabularySize(user, lang);
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            userVocabularyRepository.upsertUserVocabulary(
                    user.getId(), word.toLowerCase(), lang, LocalDateTime.now());
        }
        receive.apply(
                Content.of().text(String.format("_Вы получили награду_: *%d* \uD83D\uDC8E", words.length))
                        .chatId(user.getChatId())
                        .build()
        );
        int updatedVocabularySize = countVocabularySize(user, lang);
        if (updatedVocabularySize > currentVocabulariesSize) {
            receive.apply(
                    Content.of().text(String.format("_Поздравляю, Вы расширили словарный запас_: +*%d* 📚",
                                    updatedVocabularySize - currentVocabulariesSize))
                            .chatId(user.getChatId())
                            .build()
            );
        }
        userStatisticRepository.upsertUserStatistic(user.getId(), lang, updatedVocabularySize, 0);
    }

    private int estimateSpeechTime(String[] words) {
        int wordCount = words.length;
        double wordsPerSecond = 2.5;
        return (int) Math.round(wordCount / wordsPerSecond);
    }

    private int countVocabularySize(User user, String lang) {
        return userVocabularyRepository.findByUserAndLang(user, lang).size();
    }
}
