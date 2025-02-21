package ru.job4j.it.talk.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.it.talk.model.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByName(String name);

    Optional<User> findByClientId(@NonNull Long clientId);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ts_user (chat_id, client_id, name) "
            + "VALUES (:chatId, :clientId, :name) ON CONFLICT (client_id) DO NOTHING", nativeQuery = true)
    void create(@Param("chatId") Long chatId,
                @Param("clientId") Long clientId,
                @Param("name") String name);
}
