package ru.job4j.it.talk.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.User;
import ru.job4j.it.talk.model.Voice;
import ru.job4j.it.talk.repository.VoiceRepository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.function.Function;

@Service
@Slf4j
@AllArgsConstructor
public class Analyze {

    private final VoiceRepository voiceRepository;
    private final DailyAimService dailyAimService;

    public void processVoice(User user, Path originVoice, String text, String lang,
                             Integer messageId, String translateText, Function<Content, Integer> receive) {
        var words = text.split("[^a-zA-Z0-9]+");
        int duration = estimateSpeechTime(words);
        var voice = new Voice();
        voice.setUser(user);
        voice.setText(text);
        voice.setDuration(duration);
        voice.setCreated(LocalDateTime.now());
        voiceRepository.save(voice);
    }

    public void processText(User user, String text, String lang,
                            Integer messageId, String translateText,
                            Function<Content, Integer> receive) {
        var words = text.split("[^a-zA-Z0-9]+");
        var voice = new Voice();
        voice.setUser(user);
        voice.setText(text);
        voice.setDuration(estimateSpeechTime(words));
        voice.setCreated(LocalDateTime.now());
        voiceRepository.save(voice);
        dailyAimService.process(voice, receive);
    }

    private int estimateSpeechTime(String[] words) {
        int wordCount = words.length;
        double wordsPerSecond = 2.5;
        return (int) Math.round(wordCount / wordsPerSecond);
    }
}
