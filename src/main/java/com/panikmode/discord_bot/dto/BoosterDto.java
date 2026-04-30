package com.panikmode.discord_bot.dto;

import java.time.OffsetDateTime;

public record BoosterDto(
    String id,
    String name,
    String effectiveName,
    String effectiveAvatarUrl,
    OffsetDateTime since,
    long days

) {}

