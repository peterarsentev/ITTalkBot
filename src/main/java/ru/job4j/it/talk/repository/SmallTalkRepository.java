package ru.job4j.it.talk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.job4j.it.talk.model.SmallTalk;

@Repository
public interface SmallTalkRepository extends JpaRepository<SmallTalk, Long> {
    @Query("""
            SELECT sm FROM ts_small_talk as sm 
            ORDER BY RANDOM() LIMIT 1
            """)
    SmallTalk findRandom();
}
