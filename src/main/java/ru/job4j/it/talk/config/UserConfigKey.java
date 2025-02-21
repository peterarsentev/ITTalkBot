package ru.job4j.it.talk.config;

public enum UserConfigKey {
    TARGET_LANG(0),
    LEVEL_LANG(1),
    TOPIC_ID(2),
    QUESTION_ID(3);

    public final int key;

    UserConfigKey(int key) {
        this.key = key;
    }
}
