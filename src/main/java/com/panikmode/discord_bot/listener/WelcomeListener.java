package com.panikmode.discord_bot.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Component
public class WelcomeListener extends ListenerAdapter {

    @Value("${discord.welcome-channel-id}")
    private String welcomeChannelId;

    @Value("${discord.gifs-path:/opt/discord-bot/gifs}")
    private String gifsPath;

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        sendWelcome(event.getGuild(), event.getMember());
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equalsIgnoreCase("!testwelcome")) {
            sendWelcome(event.getGuild(), event.getMember());
        }
    }


    private void sendWelcome(Guild guild, Member member) {

        TextChannel welcomeChannel = guild.getTextChannelById(welcomeChannelId);

        if (welcomeChannel != null && member != null) {

            // Calculate account age dynamically
            OffsetDateTime createdAt = member.getUser().getTimeCreated();
            long accountAgeYears = ChronoUnit.YEARS.between(createdAt, OffsetDateTime.now());
            String accountAge = accountAgeYears > 0
                    ? accountAgeYears + " year" + (accountAgeYears > 1 ? "s" : "")
                    : "Less than a year";

            // Calculate ordinal member number (e.g., 1st, 42nd, 103rd)
            int memberCount = guild.getMemberCount();
            String ordinal = getOrdinal(memberCount);
            Role role = guild.getRoleById("861572123901034516");

            assert role != null;
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Greetings! Welcome to " + guild.getName() + "!")
                    .setDescription(
                            "Hey " + member.getAsMention() + ", we're thrilled to have you here!\n\n" +
                                    "You are our **" + ordinal + " member** — thanks for joining the community!\n\n" +
                                    ":white_check_mark: Please contact an" + role.getAsMention()  + "get your roles assigned\n\n " +
                                    ":white_check_mark: Read the rules\n" +
                                    "\n" +
                                    ":star: Say hi in **#general**"
                    )
                    .setColor(new Color(0, 176, 244))  // Discord light blue
                    .setThumbnail(member.getUser().getEffectiveAvatarUrl() + "?size=256")
                    .addField("Member", "#" + memberCount, true)
                    .addField("Joined Discord", String.valueOf(createdAt.getYear()), true)
                    .addField("Account Age", accountAge, true)
                    .setFooter("Everyone welcome "+ member.getNickname() + "!", guild.getIconUrl())
                    .setTimestamp(Instant.now());

            welcomeChannel.sendMessageEmbeds(embed.build())
                    .queue(msg -> {
                        File[] gifs = new File(gifsPath).listFiles(
                                f -> f.getName().toLowerCase().endsWith(".gif")
                        );
                        if (gifs != null && gifs.length > 0) {
                            File randomGif = gifs[new Random().nextInt(gifs.length)];
                            welcomeChannel.sendFiles(
                                    net.dv8tion.jda.api.utils.FileUpload.fromData(randomGif)
                            ).queue();
                        }
                    });

        }
    }

    // Helper: formats number as ordinal (1st, 2nd, 3rd, 42nd...)
    private String getOrdinal(int number) {
        String[] suffixes = {"th", "st", "nd", "rd"};
        int mod100 = number % 100;
        int mod10 = number % 10;
        String suffix = (mod100 >= 11 && mod100 <= 13) ? "th"
                : (mod10 < suffixes.length ? suffixes[mod10] : "th");
        return number + suffix;
    }


}
