package ru.job4j.it.talk.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import ru.job4j.it.talk.config.SslDisabling;
import ru.job4j.it.talk.service.util.GigaAuth;
import ru.job4j.it.talk.service.util.LevelLangPrompt;

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

    public String callWithoutSystem(String message, Long chatId) {
        String req = """
                            {
                              "model": "GigaChat",
                              "messages": [
                                {
                                  "role": "user",
                                  "content": "%s"
                                }
                              ],
                              "stream": false,
                              "update_interval": 0
                            }""".formatted(escapeInvalidJsonChars(message));
        return call(req, chatId);
    }

    public static void main(String[] args) throws Exception {
        SslDisabling.disableCertificateValidation();
        compare();
        /**
         * Once upon a time, there was a king who lived in a castle. He had a special room where he kept his treasures. One day, the king decided to organize his coins by size and color. He lined them up in different rows according to their order. This made the king very happy, as it helped him keep track of his wealth.
         * В чем отличие методы save от merge?
         */
    }

    private static void compare() {
        var key = "NGVkOWYyMjQtZmFlNy00YTc0LThlMDYtYWM5ZTExNDJlMGY0OjQ4OTdlM2E1LTg1MmQtNDUzNy1iNGIyLTNlZjFhZGNiZDEzYQ==";
        var req = new StringBuilder();
        // 'la' 'flu' 'choice' 'poundery'
        req.append("Оцени мой ответ на вопрос в баллах от 0 до 100. Тема: 1.2. ООП | Основы. Вопрос: Что такое наследование?. Мой ответ: Двои - это принцип создание новых классов за счет приспользования предыдущих.\n"
               + ". Формат твоего ответа: Балл: [0 до 100] ");
        var resp = new GigaChatService(new GigaAuth(key))
                .callWithoutSystem(
                        req.toString(), 1020L);
        System.out.println(resp);
    }

    private static void respInEnglish() {
        var key = "NGVkOWYyMjQtZmFlNy00YTc0LThlMDYtYWM5ZTExNDJlMGY0OjQ4OTdlM2E1LTg1MmQtNDUzNy1iNGIyLTNlZjFhZGNiZDEzYQ==";
        var req = new StringBuilder();
        // 'la' 'flu' 'choice' 'poundery'
        req.append("Create a short story in English using the words 'la', 'flu', 'choice', and 'poundery'");
        var system = new LevelLangPrompt().prompt("A1");
        var resp = new GigaChatService(new GigaAuth(key))
                .callRole(
                        system,
                        req.toString(), -1L);
        System.out.println(resp);
    }

    private static void respToEn() {
        var key = "NGVkOWYyMjQtZmFlNy00YTc0LThlMDYtYWM5ZTExNDJlMGY0OjQ4OTdlM2E1LTg1MmQtNDUzNy1iNGIyLTNlZjFhZGNiZDEzYQ==";
        var req = new StringBuilder();
        req.append("Переведи на русский.\n\n");
        req.append("What's your favorite dish?");
        var resp = new GigaChatService(new GigaAuth(key))
                .callWithoutSystem(
                        req.toString(), -1L);
        System.out.println(resp);
    }

    private static void enToRu() {
        var key = "NGVkOWYyMjQtZmFlNy00YTc0LThlMDYtYWM5ZTExNDJlMGY0OjQ4OTdlM2E1LTg1MmQtNDUzNy1iNGIyLTNlZjFhZGNiZDEzYQ==";
        var req = new StringBuilder();
        req.append("Переведи на русский.\n\n");
        var resp = new GigaChatService(new GigaAuth(key))
                .callWithoutSystem(
                        req.toString(), -1L);
        System.out.println(resp);
    }

    private static void askByEn() {
        var key = "NGVkOWYyMjQtZmFlNy00YTc0LThlMDYtYWM5ZTExNDJlMGY0OjQ4OTdlM2E1LTg1MmQtNDUzNy1iNGIyLTNlZjFhZGNiZDEzYQ==";
        var req = new StringBuilder();
        var resp = new GigaChatService(new GigaAuth(key))
                .callWithoutSystem(
                        req.toString(), -1L);
        System.out.println(resp);
    }

    private static void respToRu() {
        var key = "NGVkOWYyMjQtZmFlNy00YTc0LThlMDYtYWM5ZTExNDJlMGY0OjQ4OTdlM2E1LTg1MmQtNDUzNy1iNGIyLTNlZjFhZGNiZDEzYQ==";
        var req = new StringBuilder();
        req.append("Переведи на русский.\n\n");
        var resp = new GigaChatService(new GigaAuth(key))
                .callWithoutSystem(
                        req.toString(), -1L);
        System.out.println(resp);
    }
}

