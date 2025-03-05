package ru.job4j.it.talk.service.handle;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.service.ContentSender;
import ru.job4j.it.talk.service.GigaChatService;
import ru.job4j.it.talk.service.SpeechToText;
import ru.job4j.it.talk.service.UserService;
import ru.job4j.it.talk.service.job4j.QuestionService;
import ru.job4j.it.talk.service.job4j.TopicService;
import ru.job4j.it.talk.service.ui.Prompt;
import ru.job4j.it.talk.service.util.MarkDown;

import java.nio.file.Path;

@Service
@AllArgsConstructor
@Slf4j
public class VoiceHandle {
    private final UserService userService;
    private final GigaChatService gigaChatService;
    private final SpeechToText speechToText;
    private final TopicService topicService;
    private final QuestionService questionService;

    public void process(Long chatId,
                        Message message,
                        Path originVoice,
                        ContentSender receive) {
        var user = userService.findOrCreateUser(message);
        var analyzeMessageId = receive.sent(
                Content.of().chatId(chatId)
                        .text("🔄 _Анализирую голосовое сообщение ..._").build());
        var lang = "ru";
        var originText = speechToText.convert(originVoice, lang);
        receive.sent(
                Content.of().chatId(chatId)
                        .deleteMessageId(analyzeMessageId).build()
        );
        var resp = String.format("🗣️ *Вы [%s]*:\n%s ", lang, originText);
        receive.sent(
                Content.of().chatId(chatId).text(resp).build()
        );
        var botCallMessageId = receive.sent(
                Content.of().chatId(chatId).text("🔄 _Генерирую ответ..._").build()
        );
        var questionId = userService.findUserConfigByKey(user.getId(), UserConfigKey.QUESTION_ID);
        if (questionId.isEmpty()) {
            receive.sent(
                    Content.of()
                            .chatId(chatId)
                            .text("Выберите вопрос.")
                            .build()
            );
            return;
        }
        var question = questionService.findNavigateById(Long.parseLong(questionId.get().getValue()));
        var topic = topicService.findById(question.getQuestion().getTopicId());
        var req = new Prompt(new MarkDown()).checkAnswer(
                topic.getName(),
                question.getQuestion().getDescription(),
                originText
        );
        var text = String.format("🗣️ *Бот*:\n%s",
                gigaChatService.callWithoutSystem(req, chatId)
        );
        int maxMessageLength = 4096;
        while (text.length() > maxMessageLength) {
            String part = text.substring(0, maxMessageLength);
            receive.sent(
                    Content.of()
                            .chatId(user.getChatId())
                            .text(part)
                            .build());
            text = text.substring(maxMessageLength);
        }
        if (!text.isEmpty()) {
            receive.sent(
                    Content.of()
                            .chatId(user.getChatId())
                            .text(text)
                            .build());
        }
        receive.sent(
                Content.of()
                        .chatId(chatId)
                        .deleteMessageId(botCallMessageId)
                        .build()
        );
    }
}
