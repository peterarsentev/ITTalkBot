package ru.job4j.it.talk.service.job4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.job4j.it.talk.dto.Topic;
import ru.job4j.it.talk.dto.TopicPage;

import java.util.Collections;
import java.util.List;

@Service
public class TopicService {

    private String apiUrl;

    private final RestTemplate restTemplate;

    public TopicService(@Value("${job4j.api.url}") String apiUrl, RestTemplate restTemplate) {
        this.apiUrl = apiUrl;
        this.restTemplate = restTemplate;
    }

    public List<Topic> findAll() {
        String url = apiUrl + "interviewTopic/all";
        var headers = new HttpHeaders();
        headers.add("Accept", "application/json"); // Указываем, что ожидаем JSON
        var entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Topic>>() {
                }
        );
        return response.getBody() != null ? response.getBody() : Collections.emptyList();
    }

    public Topic findById(Long topicId) {
        String uri = UriComponentsBuilder.fromUriString(apiUrl + "interviewTopic/get")
                .queryParam("topicId", topicId)
                .queryParam("sessionId", topicId)
                .toUriString();
        var headers = new HttpHeaders();
        headers.add("Accept", "application/json"); // Указываем, что ожидаем JSON
        var entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Topic>() {
                }
        );
        return response.getBody() != null ? response.getBody() : new Topic();
    }

    public TopicPage findByPage(int page) {
        String uri = UriComponentsBuilder.fromUriString(apiUrl + "interviewTopic/byPage")
                .queryParam("page", page)
                .toUriString();
        var headers = new HttpHeaders();
        headers.add("Accept", "application/json"); // Указываем, что ожидаем JSON
        var entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<TopicPage>() {
                }
        );
        return response.getBody() != null ? response.getBody() : new TopicPage();
    }
}
