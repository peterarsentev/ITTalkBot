package ru.job4j.it.talk.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.UserConfig;
import ru.job4j.it.talk.repository.SmallTalkRepository;
import ru.job4j.it.talk.service.ui.TgButtons;

import java.nio.file.Path;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class SmallTalkService {
    private final UserService userService;
    private final SmallTalkRepository talkRepository;
    private final TgButtons tgButtons;
    private final TextToSpeech textToSpeech;
    private final StatisticService statisticService;

    public void process(Path userDir, Long chatId,
                          Message message,
                          Function<Content, Integer> receive) {
        var analyzeMessageId = receive.apply(
                Content.of().chatId(chatId)
                        .text("🔄 _Подбираю интересную тему ..._").build());
        var user = userService.findOrCreateUser(message);
        var lang = userService.findUserConfigByKey(user.getId(), UserConfigKey.TARGET_LANG)
                .orElse(new UserConfig(-1L, user, UserConfigKey.TARGET_LANG.key, "en"))
                .getValue();
        var text = talkRepository.findRandom().getTextEn();
        var botMessageId = receive.apply(
                Content.of()
                        .chatId(chatId)
                        .text(String.format("🗣️ *Бот [%s]*:\n%s", lang, text))
                        .buttons(tgButtons.hintAndTranslate())
                        .build()
        );
        receive.apply(
                Content.of().chatId(chatId)
                        .deleteMessageId(analyzeMessageId).build()
        );
        var mp3Bot = textToSpeech.process(userDir, botMessageId, text, lang);
        receive.apply(
                Content.of()
                        .chatId(chatId)
                        .voice(mp3Bot)
                        .build()
        );
        receive.apply(
                Content.of()
                        .chatId(chatId)
                        .text(String.format("🗣️ _Ответь на %s_",
                                statisticService.flagEmoji(lang)))
                        .build()
        );
    }
}
