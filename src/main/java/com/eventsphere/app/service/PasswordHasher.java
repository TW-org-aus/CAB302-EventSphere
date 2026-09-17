package com.eventsphere.app.service;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {

    private static final String PREFIX = "argon2id";
    private static final int MEMORY_KB = 19456;
    private static final int ITERATIONS = 2;
    private static final int PARALLELISM = 1;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private static final String SEPARATOR = ":";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private PasswordHasher() {
    }

    // Returns hash for Users.PasswordHash.
    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(rawPassword, salt, MEMORY_KB, ITERATIONS, PARALLELISM);

        return String.join(SEPARATOR,
                PREFIX,
                String.valueOf(MEMORY_KB),
                String.valueOf(ITERATIONS),
                String.valueOf(PARALLELISM),
                ENCODER.encodeToString(salt),
                ENCODER.encodeToString(hash));
    }


    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || rawPassword.isBlank() || storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split(SEPARATOR);
        if (parts.length != 6 || !PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            int memoryKb = Integer.parseInt(parts[1]);
            int iterations = Integer.parseInt(parts[2]);
            int parallelism = Integer.parseInt(parts[3]);
            byte[] salt = DECODER.decode(parts[4]);
            byte[] expected = DECODER.decode(parts[5]);

            byte[] actual = derive(rawPassword, salt, memoryKb, iterations, parallelism);


            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String rawPassword, byte[] salt,
                                 int memoryKb, int iterations, int parallelism) {
        Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withMemoryAsKB(memoryKb)
                .withIterations(iterations)
                .withParallelism(parallelism)
                .withSalt(salt)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);

        byte[] hash = new byte[HASH_BYTES];
        generator.generateBytes(rawPassword.toCharArray(), hash);
        return hash;
    }
}
