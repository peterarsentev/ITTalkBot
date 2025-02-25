package ru.job4j.it.talk.dto;

import lombok.Data;

@Data
public class QuestionLite {
    private String topicName;
    private Long topicId;
    private String questionTitle;
    private String questionId;
}
