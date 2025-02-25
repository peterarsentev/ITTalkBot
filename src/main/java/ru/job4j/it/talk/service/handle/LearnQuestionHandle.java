package ru.job4j.it.talk.service.handle;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.service.ContentSender;
import ru.job4j.it.talk.service.UserService;
import ru.job4j.it.talk.service.job4j.QuestionService;
import ru.job4j.it.talk.service.util.MarkDown;

@Service
@AllArgsConstructor
public class LearnQuestionHandle implements CallBackHandle {
    private final UserService userService;
    private final QuestionService questionService;
    private final MarkDown markDown;

    @Override
    public boolean check(String data) {
        return data.startsWith("learn_question_");
    }

    public void process(Update update, ContentSender receive) {
        var data = update.getCallbackQuery().getData();
        var user = userService.findByClientId(update.getCallbackQuery().getFrom().getId()).get();
        var questionId = data.substring("learn_question_".length());
        userService.saveConfig(user.getId(), UserConfigKey.QUESTION_ID, questionId);
        var question = questionService.findById(Long.parseLong(questionId));
        receive.sent(
                Content.of()
                        .chatId(user.getChatId())
                        .text("*Объяснение*")
                        .build());
        var text = markDown.extractTextFromHtml(question.getExplanation());
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
    }
}
