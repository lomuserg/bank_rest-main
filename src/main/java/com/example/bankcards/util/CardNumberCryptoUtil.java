package com.example.bankcards.util;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CardNumberCryptoUtil {

    private final StringEncryptor encryptor;

    public CardNumberCryptoUtil(
            @Value("${app.crypto.password}") String password,
            @Value("${app.crypto.algorithm}") String algorithm,
            @Value("${app.crypto.key-iterations}") String iterations,
            @Value("${app.crypto.pool-size}") String poolSize,
            @Value("${app.crypto.provider}") String providerName,
            @Value("${app.crypto.salt-generator}") String saltGenerator,
            @Value("${app.crypto.output-type}") String outputType) {

        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm(algorithm);
        config.setKeyObtentionIterations(iterations);
        config.setPoolSize(poolSize);
        config.setProviderName(providerName);
        config.setSaltGeneratorClassName(saltGenerator);
        config.setStringOutputType(outputType);

        PooledPBEStringEncryptor pooledEncryptor = new PooledPBEStringEncryptor();
        pooledEncryptor.setConfig(config);
        this.encryptor = pooledEncryptor;
    }

    public String encrypt(String cardNumber) {
        return encryptor.encrypt(cardNumber);
    }

    public String decrypt(String encryptedCardNumber) {
        return encryptor.decrypt(encryptedCardNumber);
    }
}
