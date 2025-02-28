package ru.job4j.it.talk.service.handle;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.service.ContentSender;
import ru.job4j.it.talk.service.UserService;
import ru.job4j.it.talk.service.ui.TgButtons;

@Service
@AllArgsConstructor
public class TopicHandle implements CallBackHandle {
    private final UserService userService;
    private final TgButtons tgButtons;

    @Override
    public boolean check(String data) {
        return data.startsWith("topic_") || data.startsWith("navigate_questions_");
    }

    @Override
    public void process(Update update, ContentSender receive) {
        var data = update.getCallbackQuery().getData();
        var user = userService.findByClientId(update.getCallbackQuery().getFrom().getId()).get();
        var page = 0;
        if (data.startsWith("navigate_questions_")) {
            var ids = data.substring("navigate_questions_".length()).split("_");
            var topicId = Integer.parseInt(ids[0]);
            userService.saveConfig(user.getId(), UserConfigKey.TOPIC_ID, String.valueOf(topicId));
            page = Integer.parseInt(ids[1]);
            receive.sentAsync(
                    Content.of()
                            .chatId(user.getChatId())
                            .text("*Выберите вопрос:*")
                            .updateMessageId(update.getCallbackQuery().getMessage().getMessageId())
                            .buttons(tgButtons.questionsByTopicId(topicId, page))
                            .build()
            );
        } else {
            var topicId = Integer.parseInt(data.substring("topic_".length()));
            userService.saveConfig(user.getId(), UserConfigKey.TOPIC_ID, String.valueOf(topicId));
            receive.sent(
                    Content.of()
                            .chatId(user.getChatId())
                            .deleteMessageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
            receive.sent(
                    Content.of()
                            .chatId(user.getChatId())
                            .text("*Выберите вопрос:*")
                            .buttons(tgButtons.questionsByTopicId(topicId, page))
                            .build());
        }
    }
}
