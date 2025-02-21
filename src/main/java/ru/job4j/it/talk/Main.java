package ru.job4j.it.talk;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.job4j.it.talk.service.ReceiveUpdateService;
import ru.job4j.it.talk.config.SslDisabling;

@EnableScheduling
@SpringBootApplication
public class Main {
    public static void main(String[] args) throws Exception {
        SslDisabling.disableCertificateValidation();
        SpringApplication application = new SpringApplication(Main.class);
        application.addListeners(new ApplicationPidFileWriter("./talksharp.pid"));
        application.run();
    }

    @Bean
    public CommandLineRunner commandLineRunner(ReceiveUpdateService updateService) {
        return args -> {
            var botsApi = new TelegramBotsApi(DefaultBotSession.class);
            try {
                botsApi.registerBot(updateService);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        };
    }
}