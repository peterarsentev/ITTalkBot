package ru.job4j.it.talk.service.handle;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.job4j.it.talk.service.ContentSender;

public interface CallBackHandle {

     boolean check(String data);

     void process(Update update, ContentSender receive);
}
