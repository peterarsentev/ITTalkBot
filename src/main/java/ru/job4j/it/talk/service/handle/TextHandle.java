package ru.job4j.it.talk.service.handle;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.UserConfig;
import ru.job4j.it.talk.service.*;
import ru.job4j.it.talk.service.job4j.QuestionService;
import ru.job4j.it.talk.service.job4j.TopicService;
import ru.job4j.it.talk.service.ui.Prompt;
import ru.job4j.it.talk.service.ui.TgButtons;
import ru.job4j.it.talk.service.util.LevelLangPrompt;
import ru.job4j.it.talk.service.util.MarkDown;

import java.nio.file.Path;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class TextHandle {
    private final UserService userService;
    private final TgButtons tgButtons;
    private final Analyze analyze;
    private final GigaChatService gigaChatService;
    private final QuestionService questionService;
    private final StatisticService statisticService;
    private final TopicService topicService;

    public void process(Path userDir, Message message,
                        Function<Content, Integer> receive) {
        var user = userService.findOrCreateUser(message);
        var chatId = message.getChatId();
        var text = message.getText();
        if ("/start".equalsIgnoreCase(text) || "ℹ️ О проекте".equalsIgnoreCase(text)) {
            String introMessage = "👋 ITTalkBot | IT-Тренер\n"
                    + "\n"
                    + "\uD83D\uDDE3\uFE0F Готовься к IT-собеседованиям с помощью голосовой практики!\n"
                    + "\n"
                    + "1\uFE0F⃣ Выбери тему и вопрос с IT-собеседования \uD83C\uDFA4  \n"
                    + "2\uFE0F⃣ Запиши свой голосовой ответ  \n"
                    + "3\uFE0F⃣ Я переведу аудио в текст и проанализирую твой ответ \uD83D\uDCDD  \n"
                    + "4\uFE0F⃣ Предоставлю подробную обратную связь и рекомендации \uD83E\uDDD0  \n"
                    + "\nВсего пользователей: " + userService.totalSize();
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .text(introMessage)
                            .menu(tgButtons.menu())
                            .build()
            );
        } else if ("/settings".equalsIgnoreCase(text) || "⚙️ Настройки".equalsIgnoreCase(text)) {
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .deleteMessageId(message.getMessageId())
                            .build()
            );
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .text("⚙️ *Настройки:*")
                            .buttons(tgButtons.settings())
                            .build()
            );
        } else if ("/topics".equalsIgnoreCase(text) || "💬 Темы".equalsIgnoreCase(text)) {
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .deleteMessageId(message.getMessageId())
                            .build()
            );
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .text("*Выберите тему:*")
                            .buttons(tgButtons.topics(0))
                            .build()
            );
        } else if ("/situation".equalsIgnoreCase(text) || "📝 Ситуация".equalsIgnoreCase(text)) {
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .deleteMessageId(message.getMessageId())
                            .build()
            );
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .text("*Cитуация* \n\nВыбери место. Бот опишет ситуацию в этом месте и задаст вопрос. Твоя задача — ответить на вопрос \uD83D\uDCAC.")
                            .buttons(tgButtons.situations())
                            .build()
            );
        } else if ("/statistic".equalsIgnoreCase(text) || "🏆 Рейтинг".equalsIgnoreCase(text)) {
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .deleteMessageId(message.getMessageId())
                            .build()
            );
            statisticService.process(user, receive);
        } else if ("/retell".equalsIgnoreCase(text) || "🔄 Пересказ".equalsIgnoreCase(text)) {
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .deleteMessageId(message.getMessageId())
                            .build()
            );
        } else {
            textProcess(chatId, message, receive);
        }
    }

    public void textProcess(Long chatId,
                        Message message,
                        Function<Content, Integer> receive) {
        var user = userService.findOrCreateUser(message);
        var analyzeMessageId = receive.apply(
                Content.of().chatId(chatId)
                        .text("🔄 _Анализирую сообщение ..._").build());
        var originText = message.getText();
        receive.apply(
                Content.of().chatId(chatId)
                        .deleteMessageId(analyzeMessageId).build()
        );
        var resp = String.format("🗣️ *Вы*:\n%s", originText);
        receive.apply(
                Content.of().chatId(chatId).text(resp).build()
        );
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
        var question = questionService.findNavigateById(Long.parseLong(questionId.get().getValue()));
        var topic = topicService.findById(question.getQuestion().getTopicId());
        var req = new Prompt(new MarkDown()).checkAnswer(
                topic.getName(),
                question.getQuestion().getDescription(),
                originText
        );
        var botText = gigaChatService.callWithoutSystem(req, chatId);
        receive.apply(
                Content.of()
                        .chatId(chatId)
                        .text(String.format("🗣️ *Бот*:\n%s", botText))
                        .buttons(tgButtons.answerNavigate(topic.getId(),
                                question.getPreviousId(),
                                question.getNextId()))
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
