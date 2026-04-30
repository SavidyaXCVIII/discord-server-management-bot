package com.panikmode.discord_bot.service;

import com.panikmode.discord_bot.model.ApiResponse;
import com.panikmode.discord_bot.model.User;
import com.panikmode.discord_bot.repository.UserRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.sql.Date;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Service
public class SlashCommandService {

    private final UserService userService;
    private final UserRepository userRepository;

    public SlashCommandService(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }


    public void addUserInfo(SlashCommandInteractionEvent event) {

        event.deferReply().queue();

        String name = event.getOptionsByName("user").getFirst().getAsUser().getEffectiveName();
        String discordId = event.getOptionsByName("user").getFirst().getAsUser().getId();
        String guildId = Objects.requireNonNull(event.getGuild()).getId();
        int day = Objects.requireNonNull(event.getOption("day")).getAsInt();
        int month = Objects.requireNonNull(event.getOption("month")).getAsInt();
        int year = Objects.requireNonNull(event.getOption("year")).getAsInt();

        try {

            LocalDate localDate = LocalDate.of(year, month, day);

            User newUser = User.builder()
                    .name(name)
                    .discordId(discordId)
                    .guildId(guildId)
                    .dob(Date.valueOf(localDate))
                    .build();


            Optional<User> updatedUser = userService.createUser(newUser);

            // 5. Build and send SUCCESS Embed
            if (updatedUser.isPresent()) {
                EmbedBuilder successEmbed = new EmbedBuilder()
                        .setTitle("✅ User Information Saved")
                        .setColor(Color.GREEN)
                        .setDescription("Successfully saved data for **" + name + "**.")
                        .addField("Discord ID", discordId, true)
                        .addField("Date of Birth", localDate.toString(), true)
                        .setTimestamp(Instant.now());

                event.getHook().sendMessageEmbeds(successEmbed.build()).queue();
            }

        }
        catch (DateTimeException e) {
            // Handle invalid dates (e.g., February 30th) gracefully
            EmbedBuilder dateErrorEmbed = new EmbedBuilder()
                    .setTitle("❌ Invalid Date")
                    .setColor(Color.ORANGE)
                    .setDescription("The date provided is not a real date. Please check your inputs.");

            event.getHook().sendMessageEmbeds(dateErrorEmbed.build()).queue();

        } catch (Exception e) {
            // 6. Build and send ERROR Embed
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle("❌ Error Saving User")
                    .setColor(Color.RED)
                    .setDescription("An unexpected error occurred while trying to save the user info to the database.")
                    // Optional: You can include the error message for debugging,
                    // but you might want to remove this in a public bot.
                    .setTimestamp(Instant.now());

            event.getHook().sendMessageEmbeds(errorEmbed.build()).queue();
        }
    }
}
