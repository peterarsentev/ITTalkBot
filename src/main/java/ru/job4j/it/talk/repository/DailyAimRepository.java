package ru.job4j.it.talk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.job4j.it.talk.model.DailyAim;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyAimRepository extends JpaRepository<DailyAim, Long> {
    Optional<DailyAim> findByUserIdAndCreateDate(Long userId, LocalDate createDate);
}
