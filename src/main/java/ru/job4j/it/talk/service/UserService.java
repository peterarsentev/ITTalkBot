package ru.job4j.it.talk.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.job4j.it.talk.config.UserConfigKey;
import ru.job4j.it.talk.model.User;
import ru.job4j.it.talk.model.UserConfig;
import ru.job4j.it.talk.model.UserVocabulary;
import ru.job4j.it.talk.repository.UserConfigRepository;
import ru.job4j.it.talk.repository.UserRepository;
import ru.job4j.it.talk.repository.UserVocabularyRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserConfigRepository userConfigRepository;
    private final UserVocabularyRepository userVocabularyRepository;

    public Optional<User> findByClientId(@NonNull Long clientId) {
        return userRepository.findByClientId(clientId);
    }

    public void create(User member) {
        userRepository.create(member.getChatId(), member.getClientId(), member.getName());
    }

    public void saveConfig(Long userId, UserConfigKey key, String lang) {
        userConfigRepository.saveUserConfig(userId, key.key, lang);
    }

    public Optional<UserConfig> findUserConfigByKey(Long userId, UserConfigKey key) {
        return userConfigRepository.findByUserIdAndKey(userId, key.key);
    }

    public User findOrCreateUser(Message message) {
        var client = message.getFrom();
        var userOp = findByClientId(client.getId());
        if (userOp.isEmpty()) {
            var member = new User();
            member.setName(client.getFirstName() + " " + client.getLastName());
            member.setChatId(message.getChatId());
            member.setClientId(client.getId());
            create(member);
        }
        return findByClientId(client.getId()).get();
    }

    public List<UserVocabulary> findByUserOrderByTotalAsc(User user, Limit limit) {
        return userVocabularyRepository.findByUserOrderByTotalAsc(user, limit);
    }
}
