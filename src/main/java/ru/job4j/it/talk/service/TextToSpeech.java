package ru.job4j.it.talk.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TextToSpeech {

    public void textToVoice(Path path, String lang) {
        try {
            Path textFile = Paths.get(path.getParent().toString(), path.getFileName().toString().replace(".ogg", ".txt"));
            if (!textFile.toFile().exists()) {
                System.out.println("Text file does not exist: " + path);
                return;
            }
            String outputFilePath = path.toString().replace(".ogg", "_tmp.mp3");
            String command = String.format("gtts-cli --file %s --output %s -l %s", textFile, outputFilePath, lang);
            Process process = new ProcessBuilder(command.split(" ")).start();
            if (!process.waitFor(1, TimeUnit.MINUTES)) {
                process.destroyForcibly();
            }
            String originFilePath = path.toString().replace(".ogg", ".mp3");
            command = String.format("ffmpeg -i %s -filter:a atempo=1.10 %s", outputFilePath, originFilePath);
            process = new ProcessBuilder(command.split(" ")).start();
            if (!process.waitFor(1, TimeUnit.MINUTES)) {
                process.destroyForcibly();
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error occurred while converting text to voice", e);
        }
    }

    public String replaceAsterisk(String text) {
        return text.replaceAll("\\*\\*", "");
    }

    public Path process(Path root, Integer messageId, String text, String lang) {
        try {
            Path textFile = root.resolve(messageId + ".txt");
            Files.writeString(textFile, replaceAsterisk(text), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            var mp3File = root.resolve(messageId + "_tmp.mp3");
            String command = String.format("gtts-cli --file %s --output %s -l %s -t us", textFile, mp3File, lang);
            Process process = new ProcessBuilder(command.split(" ")).start();
            boolean finishedInTime = process.waitFor(1, TimeUnit.MINUTES);
            if (!finishedInTime) {
                process.destroyForcibly();
                log.error("gtts-cli process did not finish within 1 minute and was terminated.");
            }
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                log.error("gtts-cli process exited with non-zero code: {}", exitValue);
            }
            var originFilePath = root.resolve(messageId + ".mp3");
            command = String.format("ffmpeg -i %s -filter:a atempo=1.10 %s", mp3File, originFilePath);
            process = new ProcessBuilder(command.split(" ")).start();
            if (!process.waitFor(1, TimeUnit.MINUTES)) {
                process.destroyForcibly();
            }
            return originFilePath;
        } catch (IOException | InterruptedException e) {
            log.error("Error occurred while converting text to voice", e);
        }
        return null;
    }
}
