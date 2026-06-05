package com.panikmode.discord_bot.listener;

import com.panikmode.discord_bot.service.MessageService;
import com.panikmode.discord_bot.service.SlashCommandService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;



@Component
public class SlashCommandListener  extends ListenerAdapter {


    private final SlashCommandService slashCommandService;
    private final MessageService messageService;


    public SlashCommandListener(SlashCommandService slashCommandService,
                                MessageService messageService) {
        this.slashCommandService = slashCommandService;
        this.messageService = messageService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        switch (event.getName()) {
            case "practice" -> moveToPracticeChannel(event);
            case "official" -> officialServerAnnouncement(event);
            case "adduser" -> addUser(event);
            case "announcements" -> announceBoosters(event);
            default -> event.reply("❌ Unknown command.").setEphemeral(true).queue();
        }

    }

    private void addUser(SlashCommandInteractionEvent event) {
        slashCommandService.addUserInfo(event);
    }

    private void announceBoosters(SlashCommandInteractionEvent event) {
        messageService.announceBoosters();
    }

    private void moveToPracticeChannel(SlashCommandInteractionEvent event) {
        slashCommandService.moveToPracticeChannel(event);
    }

    private void greatServerMember(SlashCommandInteractionEvent event) {
        long ping = event.getJDA().getGatewayPing();
        event.reply("🏓 Pong! Gateway ping: `" + ping + "ms`").queue();
    }

    private void officialServerAnnouncement(SlashCommandInteractionEvent event) {
        slashCommandService.officialServerAnnouncement(event);
    }


}
