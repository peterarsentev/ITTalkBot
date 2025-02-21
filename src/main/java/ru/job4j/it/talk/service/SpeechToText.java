package ru.job4j.it.talk.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SpeechToText {
    private final String whisperModule;

    public SpeechToText(@Value("${whisper.module}") String whisperModule) {
        this.whisperModule = whisperModule;
    }

    public String convert(Path path, String lang) {
        try {
            var textFile = Paths.get(path.getParent().toString(), path.getFileName().toString().replace(".ogg", ".txt"));
            String command = String.format("whisper %s -o %s --model %s --language %s -f txt",
                    path, path.getParent(), whisperModule, lang);
            Process process = new ProcessBuilder(command.split(" ")).start();
            var stop = process.waitFor(1, TimeUnit.MINUTES);
            if (!stop) {
                process.destroyForcibly();
            }
            var text = new StringBuilder();
            try (var reader = new BufferedReader(new FileReader(textFile.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    text.append(line).append("\n");
                }
            }
            return text.toString();
        } catch (IOException | InterruptedException e) {
            log.error("When process the speech to text", e);
            return "When process the speech to text";
        }
    }
}
