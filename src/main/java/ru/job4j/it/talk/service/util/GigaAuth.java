package ru.job4j.it.talk.service.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GigaAuth {
    private final String apiKey;

    @Getter
    private volatile String token;

    public GigaAuth(@Value("${giga.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public synchronized void refresh() {
        String urlString = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth";
        String authorizationHeader = "Basic " + apiKey;
        String rqUid = "d3844d70-9e2d-4347-afa6-31788601020e";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("RqUID", rqUid);
            conn.setRequestProperty("Authorization", authorizationHeader);
            conn.setDoOutput(true);
            String body = "scope=GIGACHAT_API_PERS";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response;
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    response = br.lines().collect(Collectors.joining("\n"));
                }
                var jsonObject = new JSONObject(response);
                token = jsonObject.getString("access_token");
            } else {
                String errorMsg;
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    errorMsg = br.lines().collect(Collectors.joining("\n"));
                }
                System.err.println("Error response code: " + responseCode);
                System.err.println("Error body: " + errorMsg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

