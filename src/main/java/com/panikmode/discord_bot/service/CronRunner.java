package com.panikmode.discord_bot.service;

import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CronRunner implements ApplicationRunner {

    private MessageService messageService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
//        messageService.checkAndSendBirthdayMessages();
    }
}
