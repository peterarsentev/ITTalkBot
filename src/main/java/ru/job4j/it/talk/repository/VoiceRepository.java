package ru.job4j.it.talk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.job4j.it.talk.model.Voice;

import java.util.Optional;

@Repository
public interface VoiceRepository extends JpaRepository<Voice, Long> {
    Optional<Voice> findByMessageId(Integer messageId);
}
