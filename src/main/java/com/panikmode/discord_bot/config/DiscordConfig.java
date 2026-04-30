package com.panikmode.discord_bot.config;


import com.panikmode.discord_bot.listener.SlashCommandListener;
import com.panikmode.discord_bot.listener.WelcomeListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordConfig {

    @Value("${discord.token}")
    private String discordToken;

    @Bean
    public JDA jda(WelcomeListener welcomeListener,
                   SlashCommandListener slashCommandListener) throws InterruptedException {

        JDA jda = JDABuilder.createDefault(discordToken)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                // ✅ Add ALL listeners in one place
                .addEventListeners(welcomeListener, slashCommandListener)
                .build()
                .awaitReady();

        // ✅ Register slash commands after bot is ready
        jda.updateCommands().addCommands(
                Commands.slash("practice", "Move Panik Mode members to Practice channel."),
                Commands.slash("announcements", "Announce the boosters."),
                Commands.slash("greetings", "Echoes your message back")
                        .addOption(OptionType.STRING, "message", "The message to echo", true),
                Commands.slash("adduser", "Shows your Discord user info")
                        .addOption(OptionType.USER, "user", "Please add the user to log the birthday", true)
                        .addOption(OptionType.INTEGER, "day", "day", true)
                        .addOption(OptionType.INTEGER, "month", "month", true)
                        .addOption(OptionType.INTEGER, "year", "year", true)
        ).queue();

        return jda;
    }

}
