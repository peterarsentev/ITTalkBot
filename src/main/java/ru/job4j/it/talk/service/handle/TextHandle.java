package ru.job4j.it.talk.service.handle;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.UserConfig;
import ru.job4j.it.talk.service.*;
import ru.job4j.it.talk.service.ui.TgButtons;
import ru.job4j.it.talk.service.util.LevelLangPrompt;

import java.nio.file.Path;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class TextHandle {
    private final UserService userService;
    private final TgButtons tgButtons;
    private final Analyze analyze;
    private final GigaChatService gigaChatService;
    private final TextToSpeech textToSpeech;
    private final StatisticService statisticService;
    private final RetellService retellService;
    private final SmallTalkService smalltalkService;
    private final LevelLangPrompt levelLangPrompt;

    public void process(Path userDir, Message message,
                        Function<Content, Integer> receive) {
        var user = userService.findOrCreateUser(message);
        var chatId = message.getChatId();
        var text = message.getText();
        if ("/start".equalsIgnoreCase(text) || "ℹ️ О проекте".equalsIgnoreCase(text)) {
            String introMessage = "👋 *TalkSharp | Лингво-тренер* \n\n"
                    + "🗣️ Тренируй разговорную речь на 10 языках: \uD83C\uDDFA\uD83C\uDDF8, \uD83C\uDDE9\uD83C\uDDEA, \uD83C\uDDF7\uD83C\uDDFA\n\n"
                    + "1️⃣ Отправьте мне *голосовое сообщение*🎙️\n"
                    + "2️⃣ Я проанализирую его и предоставлю *транскрипцию* 📝\n"
                    + "3️⃣ Я проверю на наличие *грамматических ошибок* 🧐\n"
                    + "4️⃣ Я сгенерирую *аудио-ответ* с голосом носителя языка 🎧\n\n"
                    + "✅ Давайте начнем!";
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .text(introMessage)
                            .menu(tgButtons.menu())
                            .build()
            );
            smalltalkService.process(userDir, chatId, message, receive);
        } else if ("/settings".equalsIgnoreCase(text) || "⚙️ Настройки".equalsIgnoreCase(text)) {
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .deleteMessageId(message.getMessageId())
                            .build()
            );
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .text("⚙️ *Настройки:*")
                            .buttons(tgButtons.settings())
                            .build()
            );
        } else if ("/small_talk".equalsIgnoreCase(text) || "💬 Поболтать".equalsIgnoreCase(text)) {
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .deleteMessageId(message.getMessageId())
                            .build()
            );
            smalltalkService.process(userDir, chatId, message, receive);
        } else if ("/situation".equalsIgnoreCase(text) || "📝 Ситуация".equalsIgnoreCase(text)) {
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .deleteMessageId(message.getMessageId())
                            .build()
            );
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .text("*Cитуация* \n\nВыбери место. Бот опишет ситуацию в этом месте и задаст вопрос. Твоя задача — ответить на вопрос \uD83D\uDCAC.")
                            .buttons(tgButtons.situations())
                            .build()
            );
        } else if ("/statistic".equalsIgnoreCase(text) || "🏆 Рейтинг".equalsIgnoreCase(text)) {
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .deleteMessageId(message.getMessageId())
                            .build()
            );
            statisticService.process(user, receive);
        } else if ("/retell".equalsIgnoreCase(text) || "🔄 Пересказ".equalsIgnoreCase(text)) {
            receive.apply(
                    Content.of()
                            .chatId(chatId)
                            .deleteMessageId(message.getMessageId())
                            .build()
            );
            retellService.process(userDir, user, receive);
        } else {
            textProcess(userDir, chatId, message, receive);
        }
    }

    public void textProcess(Path userDir, Long chatId,
                        Message message,
                        Function<Content, Integer> receive) {
        var user = userService.findOrCreateUser(message);
        var analyzeMessageId = receive.apply(
                Content.of().chatId(chatId)
                        .text("🔄 _Анализирую сообщение ..._").build());
        var lang = userService.findUserConfigByKey(user.getId(), UserConfigKey.TARGET_LANG)
                .orElse(new UserConfig(-1L, user, UserConfigKey.TARGET_LANG.key, "en"))
                .getValue();
        var level = userService.findUserConfigByKey(user.getId(), UserConfigKey.TARGET_LANG)
                .orElse(new UserConfig(-1L, user, UserConfigKey.LEVEL_LANG.key, "A1"))
                .getValue();
        var originText = message.getText();
        receive.apply(
                Content.of().chatId(chatId)
                        .deleteMessageId(analyzeMessageId).build()
        );
        var resp = String.format("🗣️ *Вы [%s]*:\n%s", lang, originText);
        var recognitionMessageId = receive.apply(
                Content.of().chatId(chatId).text(resp)
                        .buttons(tgButtons.recommendationAndTranslation()).build()
        );
        analyze.processText(user, originText, lang, recognitionMessageId, "", receive);
        var audioMessageId = receive.apply(
                Content.of().chatId(chatId).text("🎙️ _Создаю аудио..._").build()
        );
        var mp3File = textToSpeech.process(userDir, message.getMessageId(), originText, lang);
        receive.apply(
                Content.of().chatId(chatId)
                        .voice(mp3File).build()
        );
        receive.apply(
                Content.of().chatId(chatId)
                        .deleteMessageId(audioMessageId).build()
        );
        var botText = gigaChatService.callRole(levelLangPrompt.prompt(level), originText, chatId);
        var botMessageId = receive.apply(
                Content.of()
                        .chatId(chatId)
                        .text(String.format("🗣️ *Бот [%s]*:\n%s", lang, botText))
                        .build()
        );
        var audioBotMessageId = receive.apply(
                Content.of().chatId(chatId).text("🎙️ _Создаю аудио..._").build()
        );
        var mp3Bot = textToSpeech.process(userDir, botMessageId, botText, lang);
        receive.apply(
                Content.of()
                        .chatId(chatId)
                        .deleteMessageId(audioBotMessageId)
                        .build()
        );
        receive.apply(
                Content.of()
                        .chatId(chatId)
                        .voice(mp3Bot)
                        .build()
        );
    }
}
