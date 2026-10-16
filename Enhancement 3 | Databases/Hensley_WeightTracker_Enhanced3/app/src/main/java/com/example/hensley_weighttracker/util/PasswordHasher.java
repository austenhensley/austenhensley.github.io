package com.example.hensley_weighttracker.util;

import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {
    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static HashedPassword hashPassword(String password) {
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = deriveKey(password, salt);

        return new HashedPassword(
                Base64.getEncoder().encodeToString(hash),
                Base64.getEncoder().encodeToString(salt)
        );
    }

    public static boolean verifyPassword(String password, String encodedHash, String encodedSalt) {
        if (password == null || encodedHash == null || encodedSalt == null) {
            return false;
        }

        try {
            byte[] salt = Base64.getDecoder().decode(encodedSalt);
            byte[] expectedHash = Base64.getDecoder().decode(encodedHash);
            byte[] actualHash = deriveKey(password, salt);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] deriveKey(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("PBKDF2WithHmacSHA256 is not available on this device.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash password.", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static class HashedPassword {
        private final String hash;
        private final String salt;

        public HashedPassword(String hash, String salt) {
            this.hash = hash;
            this.salt = salt;
        }

        public String getHash() {
            return hash;
        }

        public String getSalt() {
            return salt;
        }
    }
}
