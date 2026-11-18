package application.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** PBKDF2 hashing is deliberately slow, so one hash is computed once and reused. */
class PasswordUtilTest {

    private static final String PASSWORD = "correct horse battery staple";
    private static String stored;

    @BeforeAll
    static void hashOnce() {
        stored = PasswordUtil.hash(PASSWORD);
    }

    @Test
    void storedFormatIsVersionedAndNeverContainsThePassword() {
        assertTrue(stored.startsWith("pbkdf2-sha256$600000$"));
        assertFalse(stored.contains(PASSWORD));
    }

    @Test
    void verifiesTheRightPasswordOnly() {
        assertTrue(PasswordUtil.verify(PASSWORD, stored));
        assertFalse(PasswordUtil.verify("correct horse battery stapl", stored));
        assertFalse(PasswordUtil.verify("", stored));
        assertFalse(PasswordUtil.verify(null, stored));
    }

    @Test
    void sameInputGetsADifferentSaltEachTime() {
        assertNotEquals(stored, PasswordUtil.hash(PASSWORD));
    }

    @Test
    void rejectsTamperedOrMalformedHashes() {
        String[] parts = stored.split("\\$");
        String tamperedHash = parts[0] + "$" + parts[1] + "$" + parts[2] + "$" + parts[2] + parts[2];
        assertFalse(PasswordUtil.verify(PASSWORD, tamperedHash));
        assertFalse(PasswordUtil.verify(PASSWORD, "pbkdf2-sha256$600000$not-base64$x"));
        assertFalse(PasswordUtil.verify(PASSWORD, "pbkdf2-sha256$0$" + parts[2] + "$" + parts[3]));
        assertFalse(PasswordUtil.verify(PASSWORD, "pbkdf2-sha256$99999999$" + parts[2] + "$" + parts[3]));
        assertFalse(PasswordUtil.verify(PASSWORD, "pbkdf2-sha256$600000$" + parts[2]));
        assertFalse(PasswordUtil.verify(PASSWORD, null));
    }

    @Test
    void plaintextLegacyValuesAreRejectedUnlessExplicitlyAllowed() {
        // HOSPITAL_ALLOW_LEGACY_PASSWORDS is not set in the test environment.
        if (!"true".equalsIgnoreCase(System.getenv("HOSPITAL_ALLOW_LEGACY_PASSWORDS"))) {
            assertFalse(PasswordUtil.verify("secret", "secret"));
        }
    }

    @Test
    void rejectsEmptyOrOversizedPasswordsWhenHashing() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash("x".repeat(1025)));
        assertFalse(PasswordUtil.verify("x".repeat(1025), stored));
    }

    @Test
    void weakerOrForeignHashesNeedRehashing() {
        assertFalse(PasswordUtil.needsRehash(stored));
        assertTrue(PasswordUtil.needsRehash(stored.replace("$600000$", "$1000$")));
        assertTrue(PasswordUtil.needsRehash("plaintext"));
        assertTrue(PasswordUtil.needsRehash(null));
    }
}
