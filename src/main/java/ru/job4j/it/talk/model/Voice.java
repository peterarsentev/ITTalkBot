package ru.job4j.it.talk.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity(name = "ts_voice")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Voice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    private String text;

    private String lang;

    private Integer duration;

    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime created = java.time.LocalDateTime.now();

    private Integer messageId;

    @Column(name = "translate_text")
    private String translateText;
}

