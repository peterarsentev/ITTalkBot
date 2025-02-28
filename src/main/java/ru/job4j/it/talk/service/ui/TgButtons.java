package ru.job4j.it.talk.service.ui;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.job4j.it.talk.dto.Question;
import ru.job4j.it.talk.service.job4j.QuestionService;
import ru.job4j.it.talk.service.job4j.TopicService;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class TgButtons {
    private final TopicService topicService;
    private final QuestionService questionService;

    public List<List<InlineKeyboardButton>> topics(int page) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        var topics = topicService.findByPage(page);
        for (var topic : topics.getTopics()) {
            keyboard.add(List.of(createBtn(topic.getName(), "topic_" + topic.getId())));
        }
        var btn = new ArrayList<InlineKeyboardButton>();
        btn.add(createBtn("↩️ Скрыть", "hide"));
        if (topics.getPage() > 0) {
            btn.add(createBtn("\u2B05️ Назад", "navigate_topic_" + (topics.getPage() - 1)));
        }
        if (topics.getPage() + 1 <= topics.getTotal()) {
            btn.add(createBtn("\u27A1️ Вперед", "navigate_topic_" + (topics.getPage() + 1)));
        }
        keyboard.add(btn);
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> questionsByTopicId(int topicId, int page) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        var questions = questionService.findByPage(topicId, page);
        var index = page * 10 + 1;
        for (var question : questions.getQuestions()) {
            keyboard.add(List.of(createBtn(index++ + ". " + question.getQuestionTitle(),
                    "question_" + question.getQuestionId())));
        }
        var btn = new ArrayList<InlineKeyboardButton>();
        btn.add(createBtn("↩️ Скрыть", "hide"));
        if (questions.getPage() > 0) {
            btn.add(createBtn("\u2B05️ Назад",
                    "navigate_questions_" + topicId + "_" + (questions.getPage() - 1)));
        }
        if (questions.getPage() + 1 <= questions.getTotal()) {
            btn.add(createBtn("\u27A1️ Вперед",
                    "navigate_questions_" + topicId + "_" + (questions.getPage() + 1)));
        }
        keyboard.add(btn);
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> levels() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createBtn("A1 - Начальный уровень", "set_level_A1"))); // Beginner level
        keyboard.add(List.of(createBtn("A2 - Элементарный уровень", "set_level_A2"))); // A2 indicating progress
        keyboard.add(List.of(createBtn("B1 - Средний уровень", "set_level_B1"))); // B1 for intermediate
        keyboard.add(List.of(createBtn("B2 - Продвинутый уровень", "set_level_B2"))); // B2 for advanced intermediate
        keyboard.add(List.of(createBtn("C1 - Профессиональный уровень", "set_level_C1"))); // C1 representing proficiency
        keyboard.addAll(hide());
        return keyboard;
    }

    public List<KeyboardRow> menu() {
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("💬 Темы"));
        row1.add(new KeyboardButton("ℹ️ О проекте"));
        row1.add(new KeyboardButton("⚙️ Настройки"));
        keyboard.add(row1);
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> settings() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createBtn("🌐 Изучаемый язык", "target_lang")));
        keyboard.add(List.of(createBtn("📊 Уровень языка", "level_lang")));
        keyboard.add(List.of(createBtn("💬 Поддержка", "support")));
        keyboard.addAll(hide());
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> translate() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createBtn("\uD83D\uDCDD Перевести", "translate")));
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> learn(Long topicId, Long questionId,
                                                  Integer previousId, Integer nextId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        var btn = new ArrayList<InlineKeyboardButton>();
        btn.add(createBtn("📌 Тема", "topic_" + topicId));
        btn.add(createBtn("🎓 Изучить", "learn_question_" + questionId));
        if (previousId != null) {
            btn.add(createBtn("⬅", "navigate_question_" + previousId));
        }
        if (nextId != null) {
            btn.add(createBtn("➡", "navigate_question_" + nextId));
        }
        keyboard.add(btn);
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> answerNavigate(Long topicId,
                                                  Integer previousId, Integer nextId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        var btn = new ArrayList<InlineKeyboardButton>();
        btn.add(createBtn("📌 Тема", "topic_" + topicId));
        if (previousId != null) {
            btn.add(createBtn("⬅", "after_answer_question_" + previousId));
        }
        if (nextId != null) {
            btn.add(createBtn("➡", "after_answer_question_" + nextId));
        }
        keyboard.add(btn);
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> hintAndTranslate() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createBtn("\uD83D\uDCA1 Подсказка", "hint"),
                createBtn("\uD83D\uDCDD Перевести", "translate")));
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> hide() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createBtn("\uD83D\uDCDD Скрыть", "hide")));
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> navigate(Long topicId, int page) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(
                createBtn("\uD83D\uDCCC К темам", "topics"), // Иконка для "К темам"
                createBtn("\u2B05️ Назад", "back"),           // Иконка для "Назад"
                createBtn("\u27A1️ Вперед", "forward")        // Иконка для "Вперед"
        ));
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> recommendationAndTranslation() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createBtn("\uD83E\uDDD0 Проверить", "recommendation"),
                createBtn("\uD83D\uDCDD Перевести", "translate")));
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> situations() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(
                createBtn("\uD83C\uDFEB Школа", "situation_school"),
                createBtn("\uD83C\uDF93 Университет", "situation_university")
        ));
        keyboard.add(List.of(
                createBtn("\uD83C\uDFE5 Больница", "situation_hospital"),
                createBtn("\uD83C\uDFC0 Стадион", "situation_stadium")
        ));
        keyboard.add(List.of(
                createBtn("\uD83C\uDFD6 Парк", "situation_park"),
                createBtn("\uD83C\uDFD7 Рынок", "situation_market")
        ));
        keyboard.add(List.of(
                createBtn("\uD83C\uDFE6 Магазин", "situation_shop"),
                createBtn("\uD83C\uDFD6 Кафе", "situation_cafe")
        ));
        keyboard.add(List.of(
                createBtn("\uD83C\uDF73 Ресторан", "situation_restaurant"),
                createBtn("\uD83C\uDFAC Кинотеатр", "situation_cinema")
        ));
        keyboard.add(List.of(
                createBtn("\uD83C\uDFA4 Театр", "situation_theater"),
                createBtn("\uD83D\uDCD6 Библиотека", "situation_library")
        ));
        keyboard.add(List.of(
                createBtn("\uD83C\uDFAE Музей", "situation_museum"),
                createBtn("\uD83D\uDEEC Аэропорт", "situation_airport")
        ));
        keyboard.add(List.of(
                createBtn("\uD83D\uDE80 Железнодорожный вокзал", "situation_train_station"),
                createBtn("\uD83D\uDE8C Автовокзал", "situation_bus_station")
        ));
        keyboard.add(List.of(
                createBtn("\uD83D\uDCB5 Банк", "situation_bank"),
                createBtn("\uD83D\uDCE8 Почта", "situation_post_office")
        ));
        keyboard.add(List.of(
                createBtn("\uD83C\uDFD6 Пляж", "situation_beach"),
                createBtn("\uD83C\uDFC3 Детский сад", "situation_kindergarten")
        ));
        keyboard.add(List.of(
                createBtn("\u26EA Церковь", "situation_church"),
                createBtn("\uD83C\uDFD1 Дворец спорта", "situation_sports_palace")
        ));
        keyboard.add(List.of(
                createBtn("\uD83D\uDCC5 Галерея", "situation_gallery"),
                createBtn("\uD83C\uDFB6 Концертный зал", "situation_concert_hall")
        ));
        keyboard.add(List.of(
                createBtn("\uD83D\uDED2 Торговый центр", "situation_shopping_center"),
                createBtn("\uD83D\uDCAA Фитнес-центр", "situation_fitness_center")
        ));
        keyboard.add(List.of(
                createBtn("\uD83D\uDC87 Салон красоты", "situation_beauty_salon"),
                createBtn("\uD83C\uF3D9 Аптека", "situation_pharmacy")
        ));
        keyboard.add(List.of(
                createBtn("\uD83D\uDEA8 Полиция", "situation_police"),
                createBtn("\uD83C\uDFF3 Мэрия", "situation_city_hall")
        ));
        return keyboard;
    }

    InlineKeyboardButton createBtn(String name, String data) {
        var inline = new InlineKeyboardButton();
        inline.setText(name);
        inline.setCallbackData(data);
        return inline;
    }
}

