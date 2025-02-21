package ru.job4j.it.talk.service.ui;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.job4j.it.talk.dto.Question;
import ru.job4j.it.talk.dto.Topic;
import ru.job4j.it.talk.service.QuestionService;
import ru.job4j.it.talk.service.TopicService;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class TgButtons {
    private final TopicService topicService;
    private final QuestionService questionService;

    public List<List<InlineKeyboardButton>> topics() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (var topic : topicService.findAll()) {
            keyboard.add(List.of(createBtn(topic.getName(), "topic_" + topic.getId())));
        }
        keyboard.addAll(hide());
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> questionsByTopicId(Long topicId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<Question> questions = questionService.findByTopicId(topicId);
        if (questions.size() > 20) {
            questions = questions.subList(0, 20);
        }
        for (var question : questions) {
            keyboard.add(List.of(createBtn(question.getTitle(), "question_" + question.getId())));
        }
        keyboard.addAll(hide());
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
//        KeyboardRow row2 = new KeyboardRow();
//        row2.add(new KeyboardButton("ℹ️ О проекте"));
//        row2.add(new KeyboardButton("🏆 Рейтинг"));
//        row2.add(new KeyboardButton("⚙️ Настройки"));
//        keyboard.add(row2);
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

    public List<List<InlineKeyboardButton>> languages() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createBtn("🇺🇸 English", "target_lang_en")));
//        keyboard.add(List.of(createBtn("🇪🇸 Español", "target_lang_es")));
//        keyboard.add(List.of(createBtn("🇫🇷 Français", "target_lang_fr")));
//        keyboard.add(List.of(createBtn("🇩🇪 Deutsch", "target_lang_de")));
//        keyboard.add(List.of(createBtn("🇮🇹 Italiano", "target_lang_it")));
//        keyboard.add(List.of(createBtn("🇵🇹 Português", "target_lang_pt")));
//        keyboard.add(List.of(createBtn("🇳🇱 Nederlands", "target_lang_nl")));
//        keyboard.add(List.of(createBtn("🇷🇺 Русский", "target_lang_ru")));
//        keyboard.add(List.of(createBtn("🇯🇵 日本語", "target_lang_ja")));
//        keyboard.add(List.of(createBtn("🇨🇳 中文", "target_lang_zh")));
        keyboard.addAll(hide());
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> translate() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createBtn("\uD83D\uDCDD Перевести", "translate")));
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

