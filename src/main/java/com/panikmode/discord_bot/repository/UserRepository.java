package com.panikmode.discord_bot.repository;

import com.panikmode.discord_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByDiscordId(String discord_id);

    // PostgreSQL-compatible query to match only the month and day
    @Query("SELECT u FROM User u WHERE EXTRACT(MONTH FROM u.dob) = :month AND EXTRACT(DAY FROM u.dob) = :day")
    List<User> findBirthdaysToday(@Param("month") int month, @Param("day") int day);


}
