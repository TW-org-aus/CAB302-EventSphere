package com.eventsphere.app.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    private static final String PASSWORD = "correct horse battery staple";

    @Test
    void hashProducesTheDocumentedStoredFormat() {
        String[] parts = PasswordHasher.hash(PASSWORD).split(":");

        assertEquals(6, parts.length);
        assertEquals("argon2id", parts[0]);
        assertEquals(19456, Integer.parseInt(parts[1]));
        assertEquals(2, Integer.parseInt(parts[2]));
        assertEquals(1, Integer.parseInt(parts[3]));
    }

    @Test
    void theSamePasswordHashesDifferentlyEachTime() {
        assertNotEquals(PasswordHasher.hash(PASSWORD), PasswordHasher.hash(PASSWORD));
    }

    @Test
    void bothHashesOfTheSamePasswordStillVerify() {
        assertTrue(PasswordHasher.verify(PASSWORD, PasswordHasher.hash(PASSWORD)));
        assertTrue(PasswordHasher.verify(PASSWORD, PasswordHasher.hash(PASSWORD)));
    }

    @Test
    void verifyRejectsTheWrongPassword() {
        String stored = PasswordHasher.hash(PASSWORD);

        assertFalse(PasswordHasher.verify("wrong", stored));
        assertFalse(PasswordHasher.verify(PASSWORD.toUpperCase(), stored));
        assertFalse(PasswordHasher.verify(PASSWORD + " ", stored));
    }

    @Test
    void verifyRejectsMissingInput() {
        String stored = PasswordHasher.hash(PASSWORD);

        assertFalse(PasswordHasher.verify(null, stored));
        assertFalse(PasswordHasher.verify("", stored));
        assertFalse(PasswordHasher.verify("   ", stored));
        assertFalse(PasswordHasher.verify(PASSWORD, null));
    }

    @Test
    void verifyRejectsAMalformedStoredHashInsteadOfThrowing() {
        assertFalse(PasswordHasher.verify(PASSWORD, ""));
        assertFalse(PasswordHasher.verify(PASSWORD, "not-a-hash"));
        assertFalse(PasswordHasher.verify(PASSWORD, "argon2id:19456:2:1"));
        assertFalse(PasswordHasher.verify(PASSWORD, "argon2id:x:2:1:c2FsdA==:aGFzaA=="));
        assertFalse(PasswordHasher.verify(PASSWORD, "argon2id:19456:2:1:!!notbase64!!:aGFzaA=="));
    }

    // Anything hashed by the old PBKDF2 version has three fields, so it must not verify.
    @Test
    void verifyRejectsAHashFromADifferentAlgorithm() {
        assertFalse(PasswordHasher.verify(PASSWORD, "210000:c2FsdA==:aGFzaA=="));
        assertFalse(PasswordHasher.verify(PASSWORD, "bcrypt:19456:2:1:c2FsdA==:aGFzaA=="));
    }

    // Proves verify derives with the parameters stored alongside the hash rather than
    // the current constants, which is what lets the cost be raised without locking users out.
    @Test
    void verifyUsesTheStoredParametersNotTheConstants() {
        String[] parts = PasswordHasher.hash(PASSWORD).split(":");
        parts[2] = "3";
        String tampered = String.join(":", parts);

        assertFalse(PasswordHasher.verify(PASSWORD, tampered));
    }

    @Test
    void hashRejectsABlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash("   "));
    }

    @Test
    void handlesUnicodeAndLongPasswords() {
        String unicode = "pa55w0rd-éü你好-🚀";
        assertTrue(PasswordHasher.verify(unicode, PasswordHasher.hash(unicode)));

        String long1 = "a".repeat(500);
        String stored = PasswordHasher.hash(long1);
        assertTrue(PasswordHasher.verify(long1, stored));
        assertFalse(PasswordHasher.verify("a".repeat(499), stored));
    }
}
