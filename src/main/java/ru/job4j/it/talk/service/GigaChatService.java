package ru.job4j.it.talk.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import ru.job4j.it.talk.config.SslDisabling;
import ru.job4j.it.talk.service.ui.Prompt;
import ru.job4j.it.talk.service.util.GigaAuth;
import ru.job4j.it.talk.service.util.LevelLangPrompt;
import ru.job4j.it.talk.service.util.MarkDown;

import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@AllArgsConstructor
public class GigaChatService {

    private final GigaAuth gigaAuth;

    public String escapeInvalidJsonChars(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder escapedString = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"':
                    escapedString.append("\\\"");
                    break;
                case '\\':
                    escapedString.append("\\\\");
                    break;
                case '\b':
                    escapedString.append("\\b");
                    break;
                case '\f':
                    escapedString.append("\\f");
                    break;
                case '\n':
                    escapedString.append("\\n");
                    break;
                case '\r':
                    escapedString.append("\\r");
                    break;
                case '\t':
                    escapedString.append("\\t");
                    break;
                default:
                    // Escape non-printable characters and control characters
                    if (c < 32 || c > 126) {
                        escapedString.append(String.format("\\u%04x", (int) c));
                    } else {
                        escapedString.append(c);
                    }
                    break;
            }
        }
        return escapedString.toString();
    }

    public String callRole(String role, String message, Long chatId) {
        String req = """
                            {
                              "model": "GigaChat",
                              "messages": [
                               { 
                                  "role": "system",
                                  "content": "%s"
                                },
                                {
                                  "role": "user",
                                  "content": "%s"
                                }
                              ],
                              "stream": false,
                              "update_interval": 0
                            }
                            """.formatted(escapeInvalidJsonChars(role), escapeInvalidJsonChars(message))
                .replaceAll("\\n", " ");
        return call(req, chatId);
    }

    private String callLogs(String userFirstMsg, String system, String userSecondMsg, Long chatId) {
        String req = """
                            {
                              "model": "GigaChat",
                              "messages": [
                               {
                                  "role": "user",
                                  "content": "%s"
                                },
                                {
                                  "role": "assistant",
                                  "content": "%s"
                                },
                                {
                                  "role": "user",
                                  "content": "%s"
                                }
                              ],
                              "stream": false,
                              "update_interval": 0
                            }
                            """.formatted(
                        escapeInvalidJsonChars(userFirstMsg),
                        escapeInvalidJsonChars(system),
                        escapeInvalidJsonChars(userSecondMsg))
                .replaceAll("\\n", " ");
        return call(req, chatId);
    }

    private String call(String req, Long chatId) {
        int trySize = 1;
        do {
            try {
                String urlString = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions";
                var url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + gigaAuth.getToken());
                if (chatId != -1) {
                    connection.setRequestProperty("X-Session-ID", chatId.toString());
                }
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = req.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                if (connection.getResponseCode() == 401) {
                    gigaAuth.refresh();
                    trySize++;
                    continue;
                }
                var response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                var json = new JSONObject(response.toString());
                var choicesArray = json.getJSONArray("choices");
                JSONObject firstChoice = choicesArray.getJSONObject(0);
                return firstChoice.getJSONObject("message")
                        .getString("content");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } while (trySize <= 3);
        return null;
    }

    public String callWithoutSystem(String text, Long chatId) {
        var messages = new JSONArray();
        var message = new JSONObject();
        message.put("role", "user");
        message.put("content", text);
        messages.put(message);
        var req = new JSONObject();
        req.put("model", "GigaChat");
        req.put("stream", false);
        req.put("update_interval", 0);
        req.put("messages", messages);
        return call(req.toString(), chatId);
    }

    public static void main(String[] args) throws Exception {
        SslDisabling.disableCertificateValidation();
        compare();
    }

    private static void compare() {
        var key = "NGVkOWYyMjQtZmFlNy00YTc0LThlMDYtYWM5ZTExNDJlMGY0OjQ4OTdlM2E1LTg1MmQtNDUzNy1iNGIyLTNlZjFhZGNiZDEzYQ==";
        var req = new Prompt(new MarkDown()).checkAnswer(
                "Базовый синтаксис",
                "Можете рассказать, что такое примитивные типы данных в Java? Какие существуют примитивные типы, каковы их характеристики (размер и диапазон значений), и в чем отличие примитивных типов от объектов?",
                "По-моему, в меню примитивную типу джава это таки маленькие числа, которые в джаву используются, например, для указания возраста."
        );
        var resp = new GigaChatService(new GigaAuth(key))
                .callWithoutSystem(
                        req, 1021L);
        System.out.println(resp);
    }
}

