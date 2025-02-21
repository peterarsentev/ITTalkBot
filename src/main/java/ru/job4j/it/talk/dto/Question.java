package ru.job4j.it.talk.dto;

import lombok.Data;

@Data
public class Question {
    private Long id;
    private String title;
    private String description;
    private Long topicId;
}
