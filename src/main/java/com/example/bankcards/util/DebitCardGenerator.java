package com.example.bankcards.util;

import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class DebitCardGenerator {
    private final Faker faker = new Faker();
    private final Random random = new Random();

    public String generateCardNumber() {
        String baseNumber = faker.business().creditCardNumber();

        long timestamp = System.currentTimeMillis();
        String uniquePart = String.valueOf(timestamp % 10000);

        String cleaned = baseNumber.replaceAll("[^0-9]", "");
        if (cleaned.length() > 12) {
            cleaned = cleaned.substring(0, 12);
        }

        return cleaned + String.format("%04d", random.nextInt(10000));
    }
}
