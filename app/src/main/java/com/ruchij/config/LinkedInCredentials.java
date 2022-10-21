package com.ruchij.config;

import com.typesafe.config.Config;

public record LinkedInCredentials(String email, String password) {

    public static LinkedInCredentials parse(Config config) {
        String email = config.getString("email");
        String password = config.getString("password");

        return new LinkedInCredentials(email, password);
    }
}
