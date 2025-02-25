package ru.job4j.it.talk.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.job4j.it.talk.content.Content;
import ru.job4j.it.talk.service.handle.CallBackHandle;
import ru.job4j.it.talk.service.handle.TextHandle;
import ru.job4j.it.talk.service.handle.VoiceHandle;
import ru.job4j.it.talk.service.util.MarkDown;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ReceiveUpdateService extends TelegramLongPollingBot
        implements ContentSender {

    private final String botName;
    private final String voiceDir;
    private final VoiceHandle voiceHandle;
    private final TextHandle textHandle;
    private final List<CallBackHandle> callbackHandles;
    private final MarkDown markDown;

    public ReceiveUpdateService(@Value("${telegram.bot.name}") String botName,
                                @Value("${telegram.bot.token}") String botToken,
                                @Value("${voice.dir}") String voiceDir,
                                VoiceHandle voiceHandle, TextHandle textHandle,
                                List<CallBackHandle> callbackHandles, MarkDown markDown) {
        super(botToken);
        this.botName = botName;
        this.voiceDir = voiceDir;
        this.voiceHandle = voiceHandle;
        this.textHandle = textHandle;
        this.callbackHandles = callbackHandles;
        this.markDown = markDown;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            for (var handle : callbackHandles) {
                if (handle.check(update.getCallbackQuery().getData())) {
                    handle.process(update, this);
                    break;
                }
            }
            return;
        }
        if (!update.hasMessage()) {
            return;
        }
        var userFolder = Path.of(voiceDir, update.getMessage().getChatId().toString());
        if (update.getMessage().hasText()) {
            textHandle.process(userFolder, update.getMessage(), this::sent);
        } else if (update.getMessage().hasVoice()) {
            voiceHandle.process(
                    update.getMessage().getChatId(),
                    update.getMessage(),
                    downloadVoice(update),
                    this::sent
            );
        }
    }

    private Path downloadVoice(Update update)  {
        try {
            var voice = update.getMessage().getVoice();
            String fileId = voice.getFileId();
            String clientId = String.valueOf(update.getMessage().getFrom().getId());
            String messageId = String.valueOf(update.getMessage().getMessageId());
            var getFile = new GetFile();
            getFile.setFileId(fileId);
            var file = execute(getFile);
            var downloadedFile = downloadFile(file.getFilePath());
            var dir = new File(voiceDir + File.separator + clientId);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            var destination = new File(dir, messageId + ".ogg");
            Files.copy(downloadedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return destination.toPath();
        } catch (Exception e) {
            log.error("Error occurred in downloading", e);
        }
        throw new IllegalStateException("Could not download voice");
    }

    public CompletableFuture<Integer> sentAsync(Content content) {
        return CompletableFuture.supplyAsync(() -> sent(content));
    }

    public Integer sent(Content content) {
        try {
            if (content.getDeleteMessageId() != null) {
                execute(
                        new DeleteMessage(
                                content.getChatId().toString(),
                                content.getDeleteMessageId())
                );
                return -1;
            } else if (content.getPinMessageId() != null) {
                var message = new PinChatMessage();
                message.setChatId(content.getChatId());
                message.setMessageId(content.getPinMessageId());
                message.setDisableNotification(true);
                execute(message);
                return message.getMessageId();
            } else if (content.getUnpinMessageId() != null) {
                var message = new UnpinChatMessage();
                message.setChatId(content.getChatId());
                message.setMessageId(content.getUpdateMessageId());
                execute(message);
                return message.getMessageId();
            } else if (content.getMenu() != null) {
                SendMessage message = new SendMessage();
                message.setChatId(content.getChatId());
                message.setText(markDown.correctPairSymbols((content.getText())));
                message.setParseMode("MarkdownV2");
                var markup = new ReplyKeyboardMarkup();
                markup.setKeyboard(content.getMenu());
                markup.setResizeKeyboard(true);
                message.setReplyMarkup(markup);
                return execute(message).getMessageId();
            } else if (content.getDeleteKeyboardMarkUpMessageId() != null) {
                var deleteMarkup = new EditMessageReplyMarkup();
                deleteMarkup.setChatId(content.getChatId());
                deleteMarkup.setReplyMarkup(null);
                deleteMarkup.setMessageId(content.getDeleteKeyboardMarkUpMessageId());
                execute(deleteMarkup);
                return -1;
            } else if (content.getUpdateMessageId() != null) {
                var updateMessage = new EditMessageText();
                updateMessage.setChatId(content.getChatId());
                if (content.getButtons() != null) {
                    updateMessage.setReplyMarkup(new InlineKeyboardMarkup(content.getButtons()));
                }
                updateMessage.setText(markDown.correctPairSymbols(content.getText()));
                updateMessage.setParseMode("MarkdownV2");
                updateMessage.setMessageId(content.getUpdateMessageId());
                execute(updateMessage);
                return -1;
            } else if (content.getVoice() != null) {
                return execute(
                        new SendVoice(
                                content.getChatId().toString(),
                                new InputFile(content.getVoice().toFile()))
                ).getMessageId();
            }
            var message = new SendMessage();
            message.setChatId(content.getChatId());
            message.setText(markDown.correctPairSymbols((content.getText())));
            message.setParseMode("MarkdownV2");
            if (content.getButtons() != null) {
                var inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.setKeyboard(content.getButtons());
                message.setReplyMarkup(inlineKeyboardMarkup);
            }
            return execute(message).getMessageId();
        } catch (TelegramApiException e) {
            if (content.getText() != null) {
                log.error("Message {}, {}", content.getText().length(), content.getText());
            }
            log.error("The error occurred sending a message", e);
            throw new IllegalStateException("The error occured senting a message");
        }
    }
}
