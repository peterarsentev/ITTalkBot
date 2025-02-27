package ru.job4j.it.talk.service.ui;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.it.talk.service.util.MarkDown;

@Service
@AllArgsConstructor
public class Prompt {
    private final MarkDown markDown;

    public String checkAnswer(String topic, String question, String answer) {
        return """
                Я готовлюсь к собеседованию на позицию Java программист.
                Проверьте правильность моего ответа и дайте рекомендации.
                Тема вопроса: '%s'.
                Мой ответ на вопрос '%s' звучит так: '%s'
                """.formatted(
                markDown.escapeMarkdownV2(topic),
                markDown.extractTextFromHtml(question),
                markDown.escapeMarkdownV2(answer)
        );
    }
}
