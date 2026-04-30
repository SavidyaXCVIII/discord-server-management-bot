package com.panikmode.discord_bot.listener;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.panikmode.discord_bot.service.MessageService;
import com.panikmode.discord_bot.service.SlashCommandService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.Widget;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class SlashCommandListener  extends ListenerAdapter {

    private static final String PRACTICE_CHANNEL_NAME = "Practice Channel Panik Mode";
    private static final String PRACTICE_ROLE_NAME = "Team Panik Mode";
    @Value("${discord.panikmode-practice-channel}")
    private String practiceChannelId;

    @Value("${discord.panikmode-tag}")
    private String tagId;

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
            case "greetings" -> greatServerMember(event);
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

        Guild guild = event.getGuild();

        if (guild == null) {

            event.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        VoiceChannel voiceChannel = guild.getVoiceChannelById(practiceChannelId);

        Role role = guild.getRoleById(tagId);

        // Get all members in the guild who have the role AND are in a voice channel
        List<Member> membersToMove = guild.getMembersWithRoles(role)
                .stream()
                .filter(member -> member.getVoiceState() != null
                        && member.getVoiceState().inAudioChannel()) // must be in a voice channel
                .toList();


        // Move each member to the practice channel
        for (Member member : membersToMove) {
            guild.moveVoiceMember(member, voiceChannel).queue(
                    success -> log.info("Moved {} to {}", member.getEffectiveName(), PRACTICE_CHANNEL_NAME),
                    error   -> log.error("Failed to move {}: {}", member.getEffectiveName(), error.getMessage())
            );
        }

        event.reply("✅ Moved `" + membersToMove.size() + "` member(s) with role `"
                + PRACTICE_ROLE_NAME + "` to `" + PRACTICE_CHANNEL_NAME + "`!").queue();

    }

    private void greatServerMember(SlashCommandInteractionEvent event) {
        long ping = event.getJDA().getGatewayPing();
        event.reply("🏓 Pong! Gateway ping: `" + ping + "ms`").queue();
    }


}
