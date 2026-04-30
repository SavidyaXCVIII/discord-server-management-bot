package com.panikmode.discord_bot.service;

import com.panikmode.discord_bot.model.User;
import com.panikmode.discord_bot.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService{

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Transactional
    public Optional<User> createUser(User user) {

        return Optional.of(userRepository.findUserByDiscordId(user.getDiscordId())
                .map(existingUser -> {
                    existingUser.setDob(user.getDob());
                    return existingUser;
                })
                .orElseGet(() -> userRepository.save(user)));

    }
}
