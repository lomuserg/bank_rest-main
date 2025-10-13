package com.example.bankcards.util;

import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class DebitCardGenerator {

    private final Faker faker = new Faker();

    public String generateCardNumber() {
        return faker.business().creditCardNumber();
    }

    public String generateVisa() {
        return faker.finance().creditCard();
    }

    public String generateMastercard() {
        return "5" + new Random().nextInt(1000000000) + new Random().nextInt(100000000);
    }
}
