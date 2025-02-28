package ru.job4j.it.talk.dto;

import lombok.Data;

import java.util.List;

@Data
public class TopicPage {
    private Long page;
    private Long total;
    private List<Topic> topics;
}
