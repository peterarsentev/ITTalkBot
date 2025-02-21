package ru.job4j.it.talk.service;

import ru.job4j.it.talk.content.Content;

public interface ContentSender {
    Integer sent(Content content);
}
