package ru.job4j.it.talk.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionNavigate {
    private Integer previousId;
    private Integer nextId;
    private Question question;
}
