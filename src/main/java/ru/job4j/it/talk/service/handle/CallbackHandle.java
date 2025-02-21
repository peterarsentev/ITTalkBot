package ru.job4j.it.talk.service.handle;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.UserConfig;
import ru.job4j.it.talk.repository.VoiceRepository;
import ru.job4j.it.talk.service.*;
import ru.job4j.it.talk.service.ui.TgButtons;
import ru.job4j.it.talk.service.util.LevelLangPrompt;
import ru.job4j.it.talk.service.util.MD5Corrector;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class CallbackHandle {
    private final UserService userService;
    private final GigaChatService gigaChatService;
    private final VoiceRepository voiceRepository;
    private final SituationService situationService;
    private final TgButtons tgButtons;
    private final LevelLangPrompt levelLangPrompt;
    private final QuestionService questionService;
    private final MD5Corrector md5Corrector;

    public void process(Path userDir, Update update, Function<Content, Integer> receive) {
        var data = update.getCallbackQuery().getData();
        var user = userService.findByClientId(update.getCallbackQuery().getFrom().getId()).get();
        var lang = userService.findUserConfigByKey(user.getId(), UserConfigKey.TARGET_LANG)
                .orElse(new UserConfig(-1L, user, UserConfigKey.TARGET_LANG.key, "en"))
                .getValue();
        var level = userService.findUserConfigByKey(user.getId(), UserConfigKey.TARGET_LANG)
                .orElse(new UserConfig(-1L, user, UserConfigKey.LEVEL_LANG.key, "A1"))
                .getValue();
        if ("hide".startsWith(data)) {
            var message = (Message) update.getCallbackQuery().getMessage();
            receive.apply(
                    Content.of().chatId(user.getChatId())
                            .deleteMessageId(message.getMessageId())
                            .build());
        } else if (data.startsWith("topic_")) {
            var topicId = data.substring("topic_".length());
            userService.saveConfig(user.getId(), UserConfigKey.TOPIC_ID, topicId);
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .deleteMessageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .text("*Выберите вопрос:*")
                            .buttons(tgButtons.questionsByTopicId(Long.valueOf(topicId)))
                            .build());
        } else if (data.startsWith("question_")) {
            var questionId = data.substring("question_".length());
            userService.saveConfig(user.getId(), UserConfigKey.QUESTION_ID, questionId);
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .deleteMessageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
            var question = questionService.findById(Long.parseLong(questionId));
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .text(String.format("*Ответьте текcтом или голосом:*\n\n%s",
                                    md5Corrector.html2mdv2(question.getDescription())))
                            .build());
        } else if (data.equalsIgnoreCase("target_lang")) {
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .text("*Выберите язык для изучения:*")
                            .buttons(tgButtons.languages())
                            .build());
        } else if (data.equalsIgnoreCase("level_lang")) {
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .deleteMessageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .text("*Выберите уровень языка:*")
                            .buttons(tgButtons.levels())
                            .build());
        } else if (data.startsWith("situation_")) {
            situationService.process(userDir, update, receive);
        }
    }
}
