package ru.job4j.it.talk.service.handle;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.service.*;
import ru.job4j.it.talk.service.job4j.QuestionService;
import ru.job4j.it.talk.service.job4j.TopicService;
import ru.job4j.it.talk.service.ui.TgButtons;
import ru.job4j.it.talk.service.util.MarkDown;

import java.nio.file.Path;
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
    private final TopicService topicService;
    private final QuestionService questionService;
    private final MarkDown md5Corrector;

    public void process(Long chatId,
                        Message message,
                        Path originVoice,
                        Function<Content, Integer> receive) {
        var user = userService.findOrCreateUser(message);
        var analyzeMessageId = receive.apply(
                Content.of().chatId(chatId)
                        .text("🔄 _Анализирую голосовое сообщение ..._").build());
        var lang = "ru";
        var originText = speechToText.convert(originVoice, lang);
        receive.apply(
                Content.of().chatId(chatId)
                        .deleteMessageId(analyzeMessageId).build()
        );
        var resp = String.format("🗣️ *Вы [%s]*:\n%s ", lang, originText);
        var recognitionMessageId = receive.apply(
                Content.of().chatId(chatId).text(resp).build()
        );
        analyze.processVoice(user, originVoice, originText, lang, recognitionMessageId, "", receive);
        var botCallMessageId = receive.apply(
                Content.of().chatId(chatId).text("🔄 _Генерирую ответ..._").build()
        );
        var questionId = userService.findUserConfigByKey(user.getId(), UserConfigKey.QUESTION_ID);
        if (questionId.isEmpty()) {
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .text("Выберите вопрос.")
                            .build()
            );
            return;
        }
        var question = questionService.findById(Long.parseLong(questionId.get().getValue()));
        var topic = topicService.findById(question.getTopicId());
        String req = """
                Я готовьлюсь к собеседованию на позицию Java программист.
                Проверьте правильность моего ответа и дайте рекомендации
                Мой ответ на вопрос '%s' звучит так:
                '%s'
                """.formatted(
                md5Corrector.extractTextFromHtml(topic.getName()),
                originText
        );
        var botText = gigaChatService.callWithoutSystem(req, chatId);
        receive.apply(
                Content.of()
                        .chatId(chatId)
                        .text(String.format("🗣️ *Бот [%s]*:\n%s", lang, botText))
                        .build()
        );
        receive.apply(
                Content.of()
                        .chatId(chatId)
                        .deleteMessageId(botCallMessageId)
                        .build()
        );
    }
}
