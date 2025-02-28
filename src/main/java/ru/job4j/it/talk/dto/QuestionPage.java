package ru.job4j.it.talk.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionPage {
    private Long page;
    private Long total;
    private Long topicId;
    private List<QuestionLite> questions;
}
