package ru.job4j.it.talk.service.handle;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.model.UserConfig;
import ru.job4j.it.talk.repository.VoiceRepository;
import ru.job4j.it.talk.service.*;
import ru.job4j.it.talk.service.ui.TgButtons;
import ru.job4j.it.talk.service.util.LevelLangPrompt;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class CallbackHandle {
    private final UserService userService;
    private final GigaChatService gigaChatService;
    private final VoiceRepository voiceRepository;
    private final SituationService situationService;
    private final TgButtons tgButtons;
    private final LevelLangPrompt levelLangPrompt;

    public void process(Path userDir, Update update, Function<Content, Integer> receive) {
        var data = update.getCallbackQuery().getData();
        var user = userService.findByClientId(update.getCallbackQuery().getFrom().getId()).get();
        var lang = userService.findUserConfigByKey(user.getId(), UserConfigKey.TARGET_LANG)
                .orElse(new UserConfig(-1L, user, UserConfigKey.TARGET_LANG.key, "en"))
                .getValue();
        var level = userService.findUserConfigByKey(user.getId(), UserConfigKey.TARGET_LANG)
                .orElse(new UserConfig(-1L, user, UserConfigKey.LEVEL_LANG.key, "A1"))
                .getValue();
        if ("recommendation".startsWith(data)) {
            var message = (Message) update.getCallbackQuery().getMessage();
            var buttons = message.getReplyMarkup().getKeyboard().iterator().next();
            var updatedBtns = buttons.stream()
                    .filter(btn -> !btn.getCallbackData().equals("recommendation"))
                    .toList();
            receive.apply(
                    Content.of().chatId(user.getChatId())
                            .deleteKeyboardMarkUpMessageId(message.getMessageId())
                            .build());
            var voice = voiceRepository.findByMessageId(message.getMessageId()).get();
            var req = new StringBuilder();
            req.append("Укажи на грамматические ошибки в английском тексте.\n\n");
            req.append(voice.getText());
            var resp = gigaChatService.callWithoutSystem(req.toString(), -1L);
            receive.apply(
                    Content.of().chatId(user.getChatId())
                            .buttons(List.of(updatedBtns))
                            .text(String.format("*Рекомендации:*\n%s", resp))
                            .build());
        } else if ("hint".startsWith(data)) {
            var message = (Message) update.getCallbackQuery().getMessage();
            var buttons = message.getReplyMarkup().getKeyboard().iterator().next();
            var updatedBtns = buttons.stream()
                    .filter(btn -> !btn.getCallbackData().equals("hint"))
                    .toList();
            receive.apply(
                    Content.of().chatId(user.getChatId())
                            .deleteKeyboardMarkUpMessageId(message.getMessageId())
                            .build());
            var generateHintId = receive.apply(Content.of().chatId(user.getChatId())
                    .text("\uD83D\uDCA1 _Создаю подсказку_ ...")
                    .build());
            var lines = message.getText().split("\n");
            receive.apply(
                    Content.of().chatId(user.getChatId())
                            .buttons(List.of(updatedBtns))
                            .updateMessageId(message.getMessageId())
                            .text(String.format("*%s*:\n%s", lines[0], lines[1]))
                            .build());
            var resp = gigaChatService.callRole(
                    levelLangPrompt.prompt(level),
                    String.format("suggest how to answer on this question and provide useful vocabulary:\n\n%s", lines[1]),
                    user.getChatId());
            receive.apply(
                    Content.of().chatId(user.getChatId())
                            .deleteMessageId(generateHintId)
                            .build());
            receive.apply(
                    Content.of().chatId(user.getChatId())
                            .buttons(List.of(updatedBtns))
                            .text(String.format("*Подсказка:*\n%s", resp))
                            .build());
        } else if ("translate".startsWith(data)) {
            var message = (Message) update.getCallbackQuery().getMessage();
            var buttons = message.getReplyMarkup().getKeyboard().iterator().next();
            var updatedBtns = buttons.stream()
                    .filter(btn -> !btn.getCallbackData().equals("translate"))
                    .toList();
            receive.apply(
                    Content.of().chatId(user.getChatId())
                            .deleteKeyboardMarkUpMessageId(message.getMessageId())
                            .build());
            var firstBreak = message.getText().indexOf("\n");
            var firstLine = message.getText().substring(0, firstBreak);
            var text = message.getText().substring(firstBreak + 1);
            var resp = gigaChatService.callWithoutSystem(
                    String.format("Переведи на русский язык\n\n%s", text), -1L);
            receive.apply(
                    Content.of().chatId(user.getChatId())
                            .buttons(List.of(updatedBtns))
                            .updateMessageId(message.getMessageId())
                            .text(String.format("*%s*:\n%s\n\n*Перевод:*\n%s", firstLine, text, resp))
                            .build());
        } else if ("hide".startsWith(data)) {
            var message = (Message) update.getCallbackQuery().getMessage();
            receive.apply(
                    Content.of().chatId(user.getChatId())
                            .deleteMessageId(message.getMessageId())
                            .build());
        } else if (data.startsWith("target_lang_")) {
            var langChange = data.substring("target_lang_".length());
            userService.saveConfig(user.getId(), UserConfigKey.TARGET_LANG, langChange);
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .deleteMessageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .text(String.format("_Установлен язык:_ %s", lang))
                            .build());
        } else if (data.startsWith("set_level_")) {
            var setLevel = data.substring("set_level_".length());
            userService.saveConfig(user.getId(), UserConfigKey.LEVEL_LANG, setLevel);
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .deleteMessageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .text(String.format("_Уровень языка:_ %s", setLevel))
                            .build());
        } else if (data.equalsIgnoreCase("target_lang")) {
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .text("*Выберите язык для изучения:*")
                            .buttons(tgButtons.languages())
                            .build());
        } else if (data.equalsIgnoreCase("level_lang")) {
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .deleteMessageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
            receive.apply(
                    Content.of()
                            .chatId(user.getChatId())
                            .text("*Выберите уровень языка:*")
                            .buttons(tgButtons.levels())
                            .build());
        } else if (data.startsWith("situation_")) {
            situationService.process(userDir, update, receive);
        }
    }
}
