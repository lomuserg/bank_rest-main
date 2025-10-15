package com.example.bankcards.util;

import com.example.bankcards.repository.CardsRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class DebitCardGenerator {
    private final Faker faker = new Faker();
    private final Random random = new Random();
    private final CardsRepository cardsRepository;

    @Value(value = "${app.card.generation.max-attempts}")
    private int maxGenerationAttempts;

    public String generateUniqueCardNumber() {
        for (int i = 0; i < maxGenerationAttempts; i++) {
            String cardNumber = generateCardNumber();
            if (isCardNumberUnique(cardNumber)) {
                return cardNumber;
            }
        }
        throw new RuntimeException("Failed to generate unique card number after " + maxGenerationAttempts + " attempts");
    }

    private String generateCardNumber() {
        String baseNumber = faker.business().creditCardNumber();

        long timestamp = System.currentTimeMillis();
        String uniquePart = String.valueOf(timestamp % 10000);

        String cleaned = baseNumber.replaceAll("[^0-9]", "");
        if (cleaned.length() > 12) {
            cleaned = cleaned.substring(0, 12);
        }

        return cleaned + String.format("%04d", random.nextInt(10000));
    }

    private boolean isCardNumberUnique(String cardNumber) {
        String cardHash = HashUtil.sha256(cardNumber);
        return !cardsRepository.existsByCardHash(cardHash);
    }
}
