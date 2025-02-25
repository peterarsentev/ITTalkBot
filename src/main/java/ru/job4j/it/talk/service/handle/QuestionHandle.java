package ru.job4j.it.talk.service.handle;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.service.ContentSender;
import ru.job4j.it.talk.service.UserService;
import ru.job4j.it.talk.service.job4j.QuestionService;
import ru.job4j.it.talk.service.ui.TgButtons;
import ru.job4j.it.talk.service.util.MarkDown;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@AllArgsConstructor
public class QuestionHandle implements CallBackHandle {
    private final UserService userService;
    private final QuestionService questionService;
    private final MarkDown markDown;
    private final TgButtons tgButtons;

    @Override
    public boolean check(String data) {
        return data.startsWith("question_");
    }

    public void process(Update update, ContentSender receive) {
        var data = update.getCallbackQuery().getData();
        var user = userService.findByClientId(update.getCallbackQuery().getFrom().getId()).get();
        var questionId = data.substring("question_".length());
        userService.saveConfig(user.getId(), UserConfigKey.QUESTION_ID, questionId);
        receive.sent(
                Content.of()
                        .chatId(user.getChatId())
                        .deleteMessageId(update.getCallbackQuery().getMessage().getMessageId())
                        .build());
        var question = questionService.findByIdShort(Long.parseLong(questionId));
        receive.sent(
                Content.of()
                        .chatId(user.getChatId())
                        .text(String.format("*Вопрос*\n\n%s",
                                markDown.extractTextFromHtml(question.getDescription())))
                        .buttons(tgButtons.learn(question.getTopicId(), question.getId()))
                        .build());
        receive.sent(
                Content.of()
                        .chatId(user.getChatId())
                        .text("_Ответьте текстом или голосом:_")
                        .build());
    }
}
