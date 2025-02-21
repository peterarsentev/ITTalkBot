package ru.job4j.it.talk.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.DailyAim;
import ru.job4j.it.talk.model.Voice;
import ru.job4j.it.talk.repository.DailyAimRepository;

import java.time.LocalDate;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class DailyAimService {
    private final DailyAimRepository dailyAimRepository;

    public void process(Voice voice, Function<Content, Integer> receive) {
        var today = LocalDate.now();
        var chatId = voice.getUser().getChatId();
        var aimOp = dailyAimRepository.findByUserIdAndCreateDate(voice.getUser().getId(), today);
        if (aimOp.isEmpty()) {
            receive.apply(Content.of()
                    .chatId(voice.getUser().getChatId())
                    .text("🎯 Ваша цель на сегодня: *10 минут* изучения!")
                    .build()
            );
            int totalDuration = 10;  // Общая длительность задачи в минутах
            double currentProgress = Math.round((double) voice.getDuration() / 60 * 100.0) / 100.0;  // Переводим секунды в минуты и округляем до 2 знаков
            int progressBarLength = 10;  // Длина прогресс-бара (в символах)
            int filledLength = (int) Math.round((currentProgress / totalDuration) * progressBarLength);  // Заполненная часть прогресс-бара
            int emptyLength = progressBarLength - filledLength;  // Пустая часть прогресс-бара
            String progressBar = "🟩".repeat(filledLength) + "⬛".repeat(emptyLength);
            var progressBarId = receive.apply(Content.of()
                    .chatId(chatId)
                    .text("🕒 " + currentProgress + "/" + totalDuration + " минут: " + progressBar)
                    .build());
            receive.apply(Content.of()
                    .chatId(chatId)
                    .pinMessageId(progressBarId)
                    .build());
            var dailyAim = new DailyAim();
            dailyAim.setUser(voice.getUser());
            dailyAim.setCreateDate(today);
            dailyAim.setDuration(10);
            dailyAim.setScope(voice.getDuration());
            dailyAim.setProgressBarMessageId(progressBarId);
            dailyAimRepository.save(dailyAim);
        } else {
            var aim = aimOp.get();
            int totalDuration = 10;  // Общая длительность задачи в минутах
            double currentProgress = Math.round(((double) (voice.getDuration() + aim.getScope()) / 60) * 100.0) / 100.0;  // Суммируем длительность и прогресс, и переводим в минуты
            int progressBarLength = 10;  // Длина прогресс-бара (в символах)
            int filledLength = (int) Math.round((currentProgress / totalDuration) * progressBarLength);
            int emptyLength = progressBarLength - filledLength;
            if (emptyLength > 0) {
                String progressBar = "🟩".repeat(filledLength) + "⬛".repeat(emptyLength);
                receive.apply(Content.of()
                        .chatId(chatId)
                        .updateMessageId(aim.getProgressBarMessageId())
                        .text("🕒 " + currentProgress + "/" + totalDuration + " минут: " + progressBar)
                        .build());
                aim.setScope(voice.getDuration() + aim.getScope());
                dailyAimRepository.save(aim);
            } else {
                receive.apply(Content.of()
                        .chatId(chatId)
                        .unpinMessageId(aim.getProgressBarMessageId())
                        .build());
            }
        }
    }
}
