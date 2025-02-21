package ru.job4j.it.talk.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.UserConfig;
import ru.job4j.it.talk.service.ui.TgButtons;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Function;

@Service
@Slf4j
@AllArgsConstructor
public class SituationService {
    private final GigaChatService gigaChatService;
    private final TextToSpeech textToSpeech;
    private final UserService userService;
    private final TgButtons tgButtons;

    public void process(Path userDir, Update update, Function<Content, Integer> receive) {
        var message = (Message) update.getCallbackQuery().getMessage();
        var data = update.getCallbackQuery().getData();
        var user = userService.findByClientId(update.getCallbackQuery().getFrom().getId()).get();
        var lang = userService.findUserConfigByKey(user.getId(), UserConfigKey.TARGET_LANG)
                .orElse(new UserConfig(-1L, user, UserConfigKey.TARGET_LANG.key, "en"))
                .getValue();
        receive.apply(
                Content.of().chatId(user.getChatId())
                        .deleteMessageId(message.getMessageId()).build());
        var analyzeMessageId = receive.apply(
                Content.of().chatId(message.getChatId())
                        .text("🔄 * Создаю ситуацию ...*").build());
        var botText = gigaChatService.callRole(
                String.format("Придумай ситуацию в %s. Задай вопрос.",
                        situations(data)), "", -1L
        );
        var botTextLang = gigaChatService.callWithoutSystem(
                String.format("Переведи на английский\n\n%s", botText), -1L);
        receive.apply(
                Content.of().chatId(user.getChatId())
                        .deleteMessageId(analyzeMessageId).build());
        var botMessageId = receive.apply(
                Content.of()
                        .chatId(message.getChatId())
                        .text(String.format("🗣️ *Бот [%s]*:\n%s", lang, botTextLang))
                        .buttons(tgButtons.translate())
                        .build()
        );
        var mp3Bot = textToSpeech.process(userDir, botMessageId, botTextLang, lang);
        receive.apply(
                Content.of()
                        .chatId(message.getChatId())
                        .voice(mp3Bot)
                        .build()
        );
    }

    public String situations(String key) {
        var locationMap = new HashMap<String, String>();
        locationMap.put("situation_school", "Школа");
        locationMap.put("situation_university", "Университет");
        locationMap.put("situation_hospital", "Больница");
        locationMap.put("situation_stadium", "Стадион");
        locationMap.put("situation_park", "Парк");
        locationMap.put("situation_market", "Рынок");
        locationMap.put("situation_shop", "Магазин");
        locationMap.put("situation_cafe", "Кафе");
        locationMap.put("situation_restaurant", "Ресторан");
        locationMap.put("situation_cinema", "Кинотеатр");
        locationMap.put("situation_theater", "Театр");
        locationMap.put("situation_library", "Библиотека");
        locationMap.put("situation_museum", "Музей");
        locationMap.put("situation_airport", "Аэропорт");
        locationMap.put("situation_train_station", "Железнодорожный вокзал");
        locationMap.put("situation_bus_station", "Автовокзал");
        locationMap.put("situation_bank", "Банк");
        locationMap.put("situation_post_office", "Почта");
        locationMap.put("situation_beach", "Пляж");
        locationMap.put("situation_kindergarten", "Детский сад");
        locationMap.put("situation_church", "Церковь");
        locationMap.put("situation_sports_palace", "Дворец спорта");
        locationMap.put("situation_gallery", "Галерея");
        locationMap.put("situation_concert_hall", "Концертный зал");
        locationMap.put("situation_shopping_center", "Торговый центр");
        locationMap.put("situation_fitness_center", "Фитнес-центр");
        locationMap.put("situation_beauty_salon", "Салон красоты");
        locationMap.put("situation_pharmacy", "Аптека");
        locationMap.put("situation_police", "Полиция");
        locationMap.put("situation_city_hall", "Мэрия");
        return locationMap.get(key);
    }
}
