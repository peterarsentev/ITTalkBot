package ru.job4j.it.talk.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.it.talk.model.UserConfig;

import java.util.List;
import java.util.Optional;

public interface UserConfigRepository extends CrudRepository<UserConfig, Long> {

    List<UserConfig> findByUserId(Long userId);

    Optional<UserConfig> findByUserIdAndKey(Long userId, int key);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ts_user_config (user_id, key, value) "
            + "VALUES (:userId, :key, :value) "
            + "ON CONFLICT (user_id, key) "
            + "DO UPDATE SET value = EXCLUDED.value", nativeQuery = true)
    void saveUserConfig(@Param("userId") Long userId,
                        @Param("key") int key,
                        @Param("value") String value);
}

