package ru.job4j.it.talk.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "ts_daily_aim")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DailyAim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(nullable = false)
    private Integer duration = 5;

    @Column(nullable = false)
    private Integer scope = 0;

    @Column(name = "progress_bar_message_id", nullable = false)
    private Integer progressBarMessageId;
}

