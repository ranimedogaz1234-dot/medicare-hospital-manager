package application.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Password hashing helpers for database credentials. */
public final class PasswordUtil {
    private static final String PREFIX = "pbkdf2-sha256$";
    private static final int ITERATIONS = 600_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int MAX_ACCEPTED_ITERATIONS = 2_000_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        if (password.isEmpty() || password.length() > 1024) throw new IllegalArgumentException("Invalid password length");
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        return PREFIX + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(derived);
    }

    public static boolean verify(String password, String storedValue) {
        if (password == null || storedValue == null || password.length() > 1024) {
            return false;
        }
        if (!storedValue.startsWith(PREFIX)) {
            return "true".equalsIgnoreCase(System.getenv("HOSPITAL_ALLOW_LEGACY_PASSWORDS")) && MessageDigest.isEqual(
                    password.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    storedValue.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        try {
            String[] parts = storedValue.split("\\$", -1);
            if (parts.length != 4 || !parts[0].equals("pbkdf2-sha256")) {
                return false;
            }
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 1 || iterations > MAX_ACCEPTED_ITERATIONS) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            if (salt.length != SALT_LENGTH_BYTES || expected.length != KEY_LENGTH_BITS / 8) return false;
            byte[] actual = derive(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static boolean needsRehash(String storedValue) {
        if (storedValue == null || !storedValue.startsWith(PREFIX)) {
            return true;
        }
        String[] parts = storedValue.split("\\$", -1);
        if (parts.length != 4) {
            return true;
        }
        try {
            return Integer.parseInt(parts[1]) < ITERATIONS;
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Password hashing is unavailable", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
