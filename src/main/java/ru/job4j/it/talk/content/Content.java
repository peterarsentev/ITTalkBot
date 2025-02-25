package ru.job4j.it.talk.content;

import lombok.Builder;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.nio.file.Path;
import java.util.List;

@Data
@Builder(builderMethodName = "of")
public class Content {
    private Long chatId;
    private String text;
    private List<List<InlineKeyboardButton>> buttons;
    private Integer deleteMessageId;
    private Path voice;
    private Integer updateMessageId;
    private Integer deleteKeyboardMarkUpMessageId;
    private List<KeyboardRow> menu;
    private Integer pinMessageId;
    private Integer unpinMessageId;
    private Integer replyMessageId;

    public static class ContentBuilder {
        public ContentBuilder textFmt(String format, Object... args) {
            this.text = String.format(format, args);
            return this;
        }
    }
}
