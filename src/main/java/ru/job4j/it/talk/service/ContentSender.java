package ru.job4j.it.talk.service;

import ru.job4j.it.talk.content.Content;

import java.util.concurrent.CompletableFuture;

public interface ContentSender {
    Integer sent(Content content);

    CompletableFuture<Integer> sentAsync(Content content);
}
