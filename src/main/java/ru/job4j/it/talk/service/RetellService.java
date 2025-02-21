package ru.job4j.it.talk.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.User;
import ru.job4j.it.talk.model.UserConfig;
import ru.job4j.it.talk.model.UserVocabulary;
import ru.job4j.it.talk.service.ui.TgButtons;
import ru.job4j.it.talk.service.util.LevelLangPrompt;

import java.nio.file.Path;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class RetellService {
    private final GigaChatService gigaChatService;
    private final TgButtons tgButtons;
    private final UserService userService;
    private final TextToSpeech textToSpeech;
    private final LevelLangPrompt levelLangPrompt;

    public void process(Path userDir, User user, Function<Content, Integer> receive) {
        var words = userService.findByUserOrderByTotalAsc(user, Limit.of(10))
                .stream().map(UserVocabulary::getWord)
                .toList();
        var lang = userService.findUserConfigByKey(user.getId(), UserConfigKey.TARGET_LANG)
                .orElse(new UserConfig(-1L, user, UserConfigKey.TARGET_LANG.key, "en"))
                .getValue();
        var level = userService.findUserConfigByKey(user.getId(), UserConfigKey.TARGET_LANG)
                .orElse(new UserConfig(-1L, user, UserConfigKey.LEVEL_LANG.key, "A1"))
                .getValue();
        var intro = new StringBuilder();
        intro.append(String.format("🗣️ *Бот [%s]*:\n", lang));
        intro.append("Я выберу несколько редких слов из твоего словаря и составлю на их основе короткую историю. История будет состоять из слов: ");
        for (String word : words) {
            intro.append("'*").append(word).append("*' ");
        }
        intro.append(".\n\n");
        intro.append("*Твоя задача — пересказать эту историю.*");
        receive.apply(Content.of()
                .chatId(user.getChatId())
                .text(intro.toString())
                .build());
        var req = new StringBuilder();
        req.append("Create a short story in English using the words: ");
        for (String word: words) {
            req.append(word).append(", ");
        }
        var rest = gigaChatService.callRole(levelLangPrompt.prompt(level), req.toString(), -1L);
        var botMessageId = receive.apply(Content.of()
                .chatId(user.getChatId())
                .text(String.format("🗣️ *Бот [%s]*:\n%s", lang, rest))
                .buttons(tgButtons.translate())
                .build());
        var audioBotMessageId = receive.apply(
                Content.of().chatId(user.getChatId()).text("🎙️ _Создаю аудио..._").build()
        );
        var mp3Bot = textToSpeech.process(userDir, botMessageId, rest, lang);
        receive.apply(
                Content.of()
                        .chatId(user.getChatId())
                        .deleteMessageId(audioBotMessageId)
                        .build()
        );
        receive.apply(
                Content.of()
                        .chatId(user.getChatId())
                        .voice(mp3Bot)
                        .build()
        );
    }
}
