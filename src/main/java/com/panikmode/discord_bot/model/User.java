package com.panikmode.discord_bot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;


@Entity
@Table(name = "DISCORD_USER")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "discord id cannot be null")
    private String discordId;

    @Column(nullable = false, unique = false, length = 250)
    @NotBlank(message = "User name is required")
    private String name;

    @Column(nullable = false, unique = false, length = 100)
    @NotBlank(message = "Guild id is required")
    private String guildId;

    @Column(nullable = false, unique = false, length = 100)
    @NotNull(message = "User birthday is required")
    private Date dob;

    private String talk() {
        return "Hi " ;
    }

}
