package com.panikmode.discord_bot.service;

import com.panikmode.discord_bot.dto.BoosterDto;
import com.panikmode.discord_bot.model.User;
import com.panikmode.discord_bot.repository.UserRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final JDA jda;
    private final UserRepository userRepository;

    public MessageService(@Lazy JDA jda, UserRepository userRepository) {
        this.jda = jda;
        this.userRepository = userRepository;
    }

    @Value("${discord.welcome-channel-id}")
    private String channelId;

    @Value("${discord.welcome-greetings-id}")
    private String greetingsChannelId;

    @Value("${discord.guild-id}")
    private String guildId;

    @Value("${discord.gifs-path:/opt/discord-bot/gifs}")
    private String gifsPath;

    private static final Color BOOST_PINK = new Color(255, 115, 250);

    public void announceBoosters() {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            CompletableFuture.failedFuture(new IllegalArgumentException("Channel not found: " + channelId));
            return;
        }

        getBoosters().thenAccept(boosters -> {

            if (boosters.isEmpty()) {
                return;
            }
            String mentions = boosters.stream().map(b -> "<@" + b.id() + ">").collect(Collectors.joining("  "));

            List<MessageEmbed> embeds = new ArrayList<>();

            embeds.add(new EmbedBuilder()
                    .setTitle("✨  S E R V E R   B O O S T E R S  ✨")
                    .setDescription(
                            "### 💖 These incredible members are keeping our server boosted! 💖\n\n" +
                                    mentions + "\n\n" +
                                    "**" + boosters.size() + "** active booster" + (boosters.size() != 1 ? "s" : "") +
                                    " — thank you so much! 🚀"
                    )
                    .setColor(BOOST_PINK)
                    .setTimestamp(Instant.now())
                    .build());

            for (BoosterDto booster : boosters) {
                long epochSec = booster.since().toEpochSecond();
                embeds.add(new EmbedBuilder()
                        .setAuthor("⬆️  " + booster.effectiveName(), null, booster.effectiveAvatarUrl())
                        .setThumbnail(booster.effectiveAvatarUrl())
                        .setDescription("<@" + booster.id() + ">  •  🕐 **" + formatDuration(booster.days()) + "**")
                        .addField("📅 Boosting Since", "<t:" + epochSec + ":D>  (<t:" + epochSec + ":R>)", false)
                        .setColor(BOOST_PINK)
                        .build());

                // Discord allows max 10 embeds per message; flush and start a new batch
                if (embeds.size() == 10) {
                    channel.sendMessageEmbeds(embeds).queue();
                    embeds.clear();
                }
            }

            if (!embeds.isEmpty()) {
                channel.sendMessageEmbeds(embeds).queue();
            }
        });
    }

    private String formatDuration(long days) {
        if (days >= 365) {
            long years = days / 365;
            long months = (days % 365) / 30;
            return years + " year" + (years > 1 ? "s" : "") +
                    (months > 0 ? " " + months + " month" + (months > 1 ? "s" : "") : "");
        } else if (days >= 30) {
            long months = days / 30;
            long rem = days % 30;
            return months + " month" + (months > 1 ? "s" : "") +
                    (rem > 0 ? " " + rem + " day" + (rem > 1 ? "s" : "") : "");
        } else {
            return days + " day" + (days != 1 ? "s" : "");
        }
    }

    public CompletableFuture<List<BoosterDto>> getBoosters() {
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            throw new IllegalStateException("Guild not found: " + guildId);
        }

        CompletableFuture<List<BoosterDto>> future = new CompletableFuture<>();
        guild.loadMembers().onSuccess(members ->
                future.complete(
                        members.stream()
                                .filter(m -> m.getTimeBoosted() != null)
                                .sorted(Comparator.comparing(Member::getTimeBoosted))
                                .map(this::toDto)
                                .toList()
                )
        ).onError(future::completeExceptionally);
        return future;
    }

    // Alternative: Fetch from Discord API
    private String fetchAvatarUrl(String discordId, JDA jda) {
        net.dv8tion.jda.api.entities.User user = jda.retrieveUserById(discordId).complete();
        return user.getEffectiveAvatarUrl();
    }

    private BoosterDto toDto(Member member) {
        OffsetDateTime since = member.getTimeBoosted() != null ? member.getTimeBoosted() : OffsetDateTime.now();
        long days = ChronoUnit.DAYS.between(since, OffsetDateTime.now());
        return new BoosterDto(
                member.getId(),
                member.getUser().getName(),
                member.getEffectiveName(),
                member.getUser().getEffectiveAvatarUrl(),
                since,
                days
        );
    }

    public CompletableFuture<net.dv8tion.jda.api.entities.User> fetchUserFuture(Long discordId) {
        CompletableFuture<net.dv8tion.jda.api.entities.User> future = new CompletableFuture<>();

        jda.retrieveUserById(discordId).queue(
                future::complete,
                future::completeExceptionally
        );

        return future;
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 7 * * *", zone = "Asia/Colombo")
    public void checkAndSendBirthdayMessages() {
        TextChannel channel = jda.getTextChannelById(greetingsChannelId);

        if (channel == null) {
            System.err.println("Channel not found with ID: " + greetingsChannelId);
            return;
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Colombo"));
        int currentMonth = today.getMonthValue();
        int currentDay = today.getDayOfMonth();

        List<User> userList = userRepository.findBirthdaysToday(currentMonth, currentDay);

        if (userList.isEmpty()) {
            return; // No birthdays today
        }

        String mentions = userList.stream()
                .map(user -> "<@" + user.getDiscordId() + ">")
                .collect(Collectors.joining("  "));

        String description;

        if (userList.size() == 1) {
            description = "### This incredible member is celebrating their birthday today! \n\n" +
                    mentions + "\n\n" +
                    "Wishing you an amazing day! 🎉";
        } else {
            description = "### These incredible members are celebrating their birthdays today! \n\n" +
                    mentions + "\n\n" +
                    "**" + userList.size() + "** birthdays — wishing you all an amazing day! 🎉";
        }

        // Send main announcement first
        MessageEmbed mainEmbed = new EmbedBuilder()
                .setTitle("🎂🎉  H A P P Y   B I R T H D A Y ! ! !  🎉🎂")
                .setDescription(
                        description
                )
                .setColor(BOOST_PINK)
                .setTimestamp(Instant.now())
                .build();

        channel.sendMessageEmbeds(List.of(mainEmbed)).queue();

        // Collect all futures and wait for them to complete
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<MessageEmbed> individualEmbeds = new ArrayList<>();

        for (User user : userList) {
            CompletableFuture<Void> future = fetchUserFuture(Long.parseLong(user.getDiscordId()))
                    .thenAccept(discordUser -> {
                        int age = calculateAge(user.getDob());

                        MessageEmbed embed = new EmbedBuilder()
                                .setAuthor("🎂  " + user.getName(), null, discordUser.getEffectiveAvatarUrl())
                                .setThumbnail(discordUser.getEffectiveAvatarUrl())
                                .setDescription("<@" + user.getDiscordId() + ">  •  🎂 **Turning " + age + " years old!** 💖")
                                .setColor(BOOST_PINK)
                                .build();

                        synchronized (individualEmbeds) {
                            individualEmbeds.add(embed);
                        }
                    });
            futures.add(future);
        }

        // Wait for all async operations to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    // Send individual embeds in batches of 10
                    if (!individualEmbeds.isEmpty()) {
                        for (int i = 0; i < individualEmbeds.size(); i += 10) {
                            int end = Math.min(i + 10, individualEmbeds.size());
                            List<MessageEmbed> batch = individualEmbeds.subList(i, end);
                            channel.sendMessageEmbeds(batch).queue();
                        }
                    }
                })
                .exceptionally(error -> {
                    System.err.println("Error sending birthday messages: " + error.getMessage());
                    return null;
                });
    }

    private int calculateAge(Date birthday) {
        if (birthday == null) return 0;

        LocalDate birthDate;

        // Check if it's java.sql.Date (doesn't support toInstant())
        if (birthday instanceof java.sql.Date) {
            birthDate = ((java.sql.Date) birthday).toLocalDate();  // ✅ Works for sql.Date
        } else {
            birthDate = birthday.toInstant()  // ✅ Works for util.Date
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        LocalDate today = LocalDate.now();

        // Handle future dates
        if (birthDate.isAfter(today)) return 0;

        return Period.between(birthDate, today).getYears();
    }

    public void sendWelcomeMessage(Guild guild, Member member) {
        TextChannel welcomeChannel = guild.getTextChannelById(channelId);

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





