package ru.job4j.it.talk.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.job4j.it.talk.dto.Question;
import ru.job4j.it.talk.dto.Topic;

import java.util.Collections;
import java.util.List;

@Service
public class QuestionService {

    private String apiUrl;

    private final RestTemplate restTemplate;

    public QuestionService(@Value("${job4j.api.url}") String apiUrl, RestTemplate restTemplate) {
        this.apiUrl = apiUrl;
        this.restTemplate = restTemplate;
    }

    public List<Question> findByTopicId(Long topicId) {
        String uri = UriComponentsBuilder.fromUriString(apiUrl + "interviewQuestion/findByTopicId")
                .queryParam("topicId", topicId)
                .toUriString();
        var headers = new HttpHeaders();
        headers.add("Accept", "application/json"); // Указываем, что ожидаем JSON
        var entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Question>>() {
                }
        );
        return response.getBody() != null ? response.getBody() : Collections.emptyList();
    }

    public Question findById(Long questionId) {
        String uri = UriComponentsBuilder.fromUriString(apiUrl + "interviewQuestion/get")
                .queryParam("questionId", questionId)
                .queryParam("sessionId", questionId)
                .toUriString();
        var headers = new HttpHeaders();
        headers.add("Accept", "application/json"); // Указываем, что ожидаем JSON
        var entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Question>() {
                }
        );
        return response.getBody() != null ? response.getBody() : new Question();
    }
}
