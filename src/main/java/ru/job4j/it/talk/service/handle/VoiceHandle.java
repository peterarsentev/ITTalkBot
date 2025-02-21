package ru.job4j.it.talk.service.handle;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.UserConfig;
import ru.job4j.it.talk.service.*;
import ru.job4j.it.talk.service.ui.TgButtons;
import ru.job4j.it.talk.service.util.LevelLangPrompt;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

@Service
@AllArgsConstructor
@Slf4j
public class VoiceHandle {
    private final UserService userService;
    private final GigaChatService gigaChatService;
    private final SpeechToText speechToText;
    private final Analyze analyze;
    private final TgButtons tgButtons;
    private final TextToSpeech textToSpeech;
    private final LevelLangPrompt levelLangPrompt;

    public void process(Long chatId,
                        Message message,
                        Path originVoice,
                        Function<Content, Integer> receive) {
        var user = userService.findOrCreateUser(message);
        var analyzeMessageId = receive.apply(
                Content.of().chatId(chatId)
                        .text("🔄 _Анализирую голосовое сообщение ..._").build());
        var lang = userService.findUserConfigByKey(user.getId(), UserConfigKey.TARGET_LANG)
                .orElse(new UserConfig(-1L, user, UserConfigKey.TARGET_LANG.key, "en"))
                .getValue();
        var level = userService.findUserConfigByKey(user.getId(), UserConfigKey.TARGET_LANG)
                .orElse(new UserConfig(-1L, user, UserConfigKey.LEVEL_LANG.key, "A1"))
                .getValue();
        var originText = speechToText.convert(originVoice, lang);
        receive.apply(
                Content.of().chatId(chatId)
                        .deleteMessageId(analyzeMessageId).build()
        );
        var resp = String.format("🗣️ *Вы [%s]*:\n%s ", lang, originText);
        var recognitionMessageId = receive.apply(
                Content.of().chatId(chatId).text(resp)
                        .buttons(tgButtons.recommendationAndTranslation()).build()
        );
        analyze.processVoice(user, originVoice, originText, lang, recognitionMessageId, "", receive);
        var audioMessageId = receive.apply(
                Content.of().chatId(chatId).text("🎙️ _Создаю аудио..._").build()
        );
        textToSpeech.textToVoice(originVoice, lang);
        var mp3File = Paths.get(originVoice.getParent().toString(),
                originVoice.getFileName().toString().replace(".ogg", ".mp3"));
        receive.apply(
                Content.of().chatId(chatId)
                        .voice(mp3File).build()
        );
        receive.apply(
                Content.of().chatId(chatId)
                        .deleteMessageId(audioMessageId).build()
        );
        var botCallMessageId = receive.apply(
                Content.of().chatId(chatId).text("🔄 _Генерирую ответ..._").build()
        );
        var botText = gigaChatService.callRole(levelLangPrompt.prompt(level), originText, chatId);
        var botMessageId = receive.apply(
                Content.of()
                        .chatId(chatId)
                        .text(String.format("🗣️ *Бот [%s]*:\n%s", lang, botText))
                        .buttons(tgButtons.translate())
                        .build()
        );
        receive.apply(
                Content.of()
                        .chatId(chatId)
                        .deleteMessageId(botCallMessageId)
                        .build()
        );
        var audioBotMessageId = receive.apply(
                Content.of().chatId(chatId).text("🎙️ _Создаю аудио..._").build()
        );
        var botAudio = textToSpeech.process(originVoice.getParent(), botMessageId, botText, lang);
        receive.apply(
                Content.of()
                        .chatId(chatId)
                        .voice(botAudio)
                        .build()
        );
        receive.apply(
                Content.of()
                        .chatId(chatId)
                        .deleteMessageId(audioBotMessageId)
                        .build()
        );
    }
}
