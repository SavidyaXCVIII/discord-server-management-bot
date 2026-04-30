package com.panikmode.discord_bot.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        String operation, // custom for custom messages
        @NotBlank String channelId,    // which channel to send to
        String message,      // plain text message (optional)
        boolean useEmbed,    // true = send as embed
        String embedTitle,   // embed title (optional)
        String embedDescription  // embed body (optional)
) {}
