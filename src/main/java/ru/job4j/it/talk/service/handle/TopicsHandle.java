package ru.job4j.it.talk.service.handle;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.service.ContentSender;
import ru.job4j.it.talk.service.UserService;
import ru.job4j.it.talk.service.ui.TgButtons;

@Service
@AllArgsConstructor
public class TopicsHandle implements CallBackHandle {
    private final UserService userService;
    private final TgButtons tgButtons;

    @Override
    public boolean check(String data) {
        return "topics".equals(data);
    }

    @Override
    public void process(Update update, ContentSender receive) {
        var data = update.getCallbackQuery().getData();
        var user = userService.findByClientId(update.getCallbackQuery().getFrom().getId()).get();
        receive.sentAsync(
                Content.of()
                        .chatId(user.getChatId())
                        .text("*Выберите тему:*")
                        .buttons(tgButtons.topics())
                        .build()
        );
    }
}
