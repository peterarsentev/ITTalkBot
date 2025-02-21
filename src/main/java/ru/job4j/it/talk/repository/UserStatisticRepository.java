package ru.job4j.it.talk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.it.talk.model.User;
import ru.job4j.it.talk.model.UserStatistic;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatisticRepository extends JpaRepository<UserStatistic, Long> {

    List<UserStatistic> findAllByOrderByVocabularySizeDesc();

    Optional<UserStatistic> findByUserAndLang(User user, String lang);

    // Custom method using native SQL to handle conflict and update spentTime and vocabularySize
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ts_user_statistic (user_id, lang, vocabulary_size, spent_time) "
            + "VALUES (:userId, :lang, :vocabularySize, :spentTime) "
            + "ON CONFLICT (user_id, lang) "
            + "DO UPDATE SET vocabulary_size = :vocabularySize, spent_time = ts_user_statistic.spent_time + :spentTime", nativeQuery = true)
    void upsertUserStatistic(Long userId, String lang, Integer vocabularySize, Integer spentTime);
}

