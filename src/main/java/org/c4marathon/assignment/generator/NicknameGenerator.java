package org.c4marathon.assignment.generator;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NicknameGenerator {

    private final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final SecureRandom RANDOM = new SecureRandom();

    public String generateNickname(String name, String email) {
        // Remove any non-English letters and numbers from the name
        String cleanedName = name.replaceAll("[^A-Za-z0-9]", "");
        // Remove @ and anything after it from the email
        String cleanedEmail = email.split("@")[0].replaceAll("[^A-Za-z0-9]", "");

        // Interleave characters from cleanedName and cleanedEmail
        StringBuilder interleaved = new StringBuilder();
        int maxLength = Math.max(cleanedName.length(), cleanedEmail.length());
        for (int i = 0; i < maxLength; i++) {
            if (i < cleanedName.length()) {
                interleaved.append(cleanedName.charAt(i));
            }
            if (i < cleanedEmail.length()) {
                interleaved.append(cleanedEmail.charAt(i));
            }
        }

        // Generate a random salt value
        String salt = RANDOM.ints(8, 0, CHARACTERS.length())
                .mapToObj(i -> String.valueOf(CHARACTERS.charAt(i)))
                .collect(Collectors.joining());

        // Combine interleaved string with salt
        String combined = interleaved.toString() + salt;

        // Shuffle the combined string
        List<Character> characters = combined.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        java.util.Collections.shuffle(characters);
        String shuffled = characters.stream().map(String::valueOf).collect(Collectors.joining());

        // Ensure nickname is exactly 8 characters long
        if (shuffled.length() > 8) {
            shuffled = shuffled.substring(0, 8);
        } else if (shuffled.length() < 8) {
            shuffled = (shuffled + salt).substring(0, 8);
        }

        return shuffled;
    }
}
