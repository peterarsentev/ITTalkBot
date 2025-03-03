package ru.job4j.it.talk.service.ui;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.it.talk.service.util.MarkDown;

import java.util.List;

@Service
@AllArgsConstructor
public class Prompt {
    private final MarkDown markDown;

    public List<String> checkAnswer(String topic, String question, String answer) {
        return List.of(
                "Я готовлюсь к собеседованию на позицию Java программист.",
                String.format("Тема: '%s'", markDown.escapeMarkdownV2(topic)),
                String.format("Вопрос: '%s'", markDown.html2md(question)),
                String.format("Мой ответ: '%s'", markDown.escapeMarkdownV2(answer)),
                "Оцени мой ответ. Дай рекомендации, что улучшить в ответе."
        );
    }
}
