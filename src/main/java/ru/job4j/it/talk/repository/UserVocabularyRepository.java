package ru.job4j.it.talk.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.it.talk.model.User;
import ru.job4j.it.talk.model.UserVocabulary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserVocabularyRepository extends JpaRepository<UserVocabulary, Long> {

    Optional<UserVocabulary> findByUserAndWordAndLang(User user, String word, String lang);

    List<UserVocabulary> findByUserAndLang(User user, String lang);

    // Custom method using native SQL to handle conflict and update usage
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ts_user_vocabulary (user_id, word, lang, created, total) "
            + "VALUES (:userId, :word, :lang, :created, 1) "
            + "ON CONFLICT (user_id, word, lang) "
            + "DO UPDATE SET total = ts_user_vocabulary.total + 1", nativeQuery = true)
    void upsertUserVocabulary(Long userId, String word, String lang, LocalDateTime created);

    List<UserVocabulary> findByUserOrderByTotalAsc(User user, Limit limit);
}