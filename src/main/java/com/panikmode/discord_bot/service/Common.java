package com.panikmode.discord_bot.service;

public interface Common {

    void addPayment();
    void addToCart();
    default void getUser() {
        System.out.println("Default implementation");
    }
}

