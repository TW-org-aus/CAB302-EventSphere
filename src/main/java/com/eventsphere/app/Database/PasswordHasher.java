package com.eventsphere.app.Database;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

// The argon2 setting, the higher the values the longer it takes to hash which means harder for attacker
public final class PasswordHasher {
    private static final int MEMORY_KB = 19 * 1024; //memory per hash (recommended by OWASP cheat sheet)
    private static final int ITERATIONS = 2; // number of passes over the memory
    private static final int PARALLELISM = 1; // the amount of parallel lanes the work gets split

    private static final int SALT_LENGTH = 16; // amount of bytes of salt per password
    private static final int HASH_LENGTH = 32; // bytes of hash
    // secure random so salts stay unpredictable not based of time
    private static final SecureRandom RANDOM = new SecureRandom();
    // salt and hash are bytes so base64 fits them to text
    // stripped padding because standard Argon2 form has no = on the end
    private static final Base64.Encoder ENCODER = Base64.getEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    // turns plain text password into a string safe so it can be stored in database
    // each call gives new salt, so hashing the same password multiple times gives different results
    public String hash(String plainPassword) {
        // in case of no password, show message
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // new random salt for the password, this stops users who have the same password
        // from having the same hash
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        // make slow on purpose
        byte[] hash = derive(plainPassword, salt, MEMORY_KB, ITERATIONS, PARALLELISM, HASH_LENGTH);

        // build the standard Argon2 string, salt stored as well as verification will need it later
        return "$argon2id$v=" + Argon2Parameters.ARGON2_VERSION_13 + "$m=" + MEMORY_KB +
                ",t=" + ITERATIONS + ",p=" + PARALLELISM + "$" + ENCODER.encodeToString(salt) +
                "$" + ENCODER.encodeToString(hash);

    }

    // validates the plain text password against the stored hash, returns false if
    // value is damaged or not an Argon2 string
    public boolean verification(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty() || hashedPassword == null) {
            return false;
        }

        // split the stored string back into its separate parts
        String[] parts = hashedPassword.split("\\$");
        if (parts.length != 6 || !"argon2id".equals(parts[1])) {
            return false; // if wrong return false
        }

        try {
            // the version must be the one we produce, different ones hash different
            if (!parts[2].startsWith("v=")) {
                return false;
            }

            int version = Integer.parseInt(parts[2].substring(2));
            if (version != Argon2Parameters.ARGON2_VERSION_13) {
                return false;
            }

            // use the settings from the stored string, then hash is rechecked with the creation settings
            // the ones above are only for new hashs
            int memoryKb = -1;
            int iterations = -1;
            int parallelism = -1;
            for (String option : parts[3].split(",")) {
                String[] optionParts = option.split("=", 2); // limit 2, split on the first =
                if (optionParts.length != 2) {
                    return false;
                }

                int value =  Integer.parseInt(optionParts[1]);
                switch (optionParts[0]) {
                    case "m" -> memoryKb = value; // memory
                    case "t" -> iterations = value; // passes
                    case "p" -> parallelism = value; // lanes
                    default -> {
                        return false; // unknown setting -> fail
                    }
                }
            }

            // this is a last sanity check, a corrupted or edited row could ask Argon2 for gigabytes
            // of memory or hundreds of passes. 8 * parallelism floor is Argon2's minimum, 1024 * 1024 is the maximum
            if (parallelism < 1 || parallelism > 64 || iterations < 1 || iterations > 100
            || memoryKb < 8 * parallelism || memoryKb > 1024 * 1024) {
                return false;
            }

            // turn salt and hash back to bytes
            byte[] salt = DECODER.decode(parts[4]);
            byte[] expectedHash = DECODER.decode(parts[5]);
            if (salt.length < 8 || expectedHash.length < 16) {
                return false; // reject if too short
            }

            // redo same calculations hash did, usings the same salt and settings
            // same password in = same bytes out
            byte[] actual = derive(plainPassword, salt, memoryKb, iterations, parallelism,
                    expectedHash.length);
            try {
                // using isEqual so that it compares every byte regardless of if there
                // is a mismatch or not, this stops the time taken being used to see how close the password is
                return MessageDigest.isEqual(expectedHash, actual);
            } finally {
                // wipe the working copy of the hash from memory when done
                Arrays.fill(actual, (byte) 0);
            }
        } catch (IllegalArgumentException | IllegalStateException ex)  {
            // bad number, base64, or settings gets refused.
            return false;
        }
    }

    // the actual Argon2 calculation, everything it uses gets passed in so that verification
    // can use it with the past settings, and so that hash can use it with current settings.
    private static byte[] derive(String password, byte[] salt, int memoryKb, int iterations,
                                 int parallelism, int length) {
        // collect the settings that bouncycastle requires, Argon_id is the variant that was recommended and used
        Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13).withSalt(salt).withMemoryAsKB(memoryKb)
                .withIterations(iterations).withParallelism(parallelism).build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);

        // UTF-8 so that no matter what machine the password give the same bytes
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[length]; // this array gets populated rather than generateBytes returning one
        try {
            generator.generateBytes(passwordBytes, out);
        } finally {
            // when does with the plain text password bytes, they get overwritten
            Arrays.fill(passwordBytes, (byte) 0);
        }
        return out;
    }
}
