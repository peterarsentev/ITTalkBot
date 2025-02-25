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
        return data.startsWith("topic_");
    }

    @Override
    public void process(Update update, ContentSender receive) {
        var data = update.getCallbackQuery().getData();
        var user = userService.findByClientId(update.getCallbackQuery().getFrom().getId()).get();
        var topicId = data.substring("topic_".length());
        userService.saveConfig(user.getId(), UserConfigKey.TOPIC_ID, topicId);
        receive.sent(
                Content.of()
                        .chatId(user.getChatId())
                        .deleteMessageId(update.getCallbackQuery().getMessage().getMessageId())
                        .build());
        receive.sent(
                Content.of()
                        .chatId(user.getChatId())
                        .text("*Выберите вопрос:*")
                        .buttons(tgButtons.questionsByTopicId(Long.valueOf(topicId)))
                        .build());
    }
}
