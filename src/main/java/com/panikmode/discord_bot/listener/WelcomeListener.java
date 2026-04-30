package com.panikmode.discord_bot.listener;

import com.panikmode.discord_bot.service.MessageService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class WelcomeListener extends ListenerAdapter {

    private final MessageService messageService;


    public WelcomeListener(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        messageService.sendWelcomeMessage(event.getGuild(), event.getMember());
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equalsIgnoreCase("!testwelcome")) {
            // enable this for testing purposes
//            messageService.sendWelcomeMessage(event.getGuild(), event.getMember());
        }
    }



}
