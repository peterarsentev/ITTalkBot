package ru.job4j.it.talk.service.handle;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.service.ContentSender;

@Service
@AllArgsConstructor
public class HintHandle implements CallBackHandle {
    @Override
    public boolean check(String data) {
        return "hide".equals(data);
    }

    @Override
    public void process(Update update, ContentSender receive) {
        var message = (Message) update.getCallbackQuery().getMessage();
        receive.sentAsync(
                Content.of().chatId(message.getChatId())
                        .deleteMessageId(message.getMessageId())
                        .build());
    }
}
