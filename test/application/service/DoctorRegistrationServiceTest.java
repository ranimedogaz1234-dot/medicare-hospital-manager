package application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * Input validation of doctor sign-up. Every case here is rejected before the database is
 * touched, so no MySQL server is needed.
 */
class DoctorRegistrationServiceTest {

    private static final String GOOD_PASSWORD = "a-long-unique-passphrase";
    private final DoctorRegistrationService service = new DoctorRegistrationService();

    private DoctorRegistrationService.Result register(String name, String email, String specialty,
                                                      String password, String confirm) {
        return service.register(name, email, specialty, password, confirm);
    }

    @Test
    void allFieldsAreRequired() {
        var r = register("", "a@b.tn", "Cardiologie", GOOD_PASSWORD, GOOD_PASSWORD);
        assertFalse(r.success());
        assertEquals("Veuillez remplir tous les champs.", r.message());
        assertFalse(register("Amel Ben Ali", "a@b.tn", "Cardiologie", GOOD_PASSWORD, null).success());
    }

    @Test
    void fullNameNeedsFirstAndLastNameAndNoControlCharacters() {
        assertFalse(register("Amel", "a@b.tn", "Cardiologie", GOOD_PASSWORD, GOOD_PASSWORD).success());
        assertFalse(register("Amel\tBen Ali", "a@b.tn", "Cardiologie", GOOD_PASSWORD, GOOD_PASSWORD).success());
        assertFalse(register("A " + "b".repeat(150), "a@b.tn", "Cardiologie", GOOD_PASSWORD, GOOD_PASSWORD).success());
    }

    @Test
    void emailMustBeValid() {
        for (String email : new String[] {"not-an-email", "a@b", ".a@b.tn", "a..b@c.tn", "a.@b.tn", "a@-b.tn"}) {
            var r = register("Amel Ben Ali", email, "Cardiologie", GOOD_PASSWORD, GOOD_PASSWORD);
            assertFalse(r.success(), email);
            assertEquals("Indiquez une adresse email valide.", r.message(), email);
        }
    }

    @Test
    void specialtyIsLimitedTo100Characters() {
        assertFalse(register("Amel Ben Ali", "a@b.tn", "x".repeat(101), GOOD_PASSWORD, GOOD_PASSWORD).success());
    }

    @Test
    void passwordMustBe12To128CharactersAndMatchConfirmation() {
        assertEquals("Utilisez un mot de passe unique de 12 à 128 caractères.",
                register("Amel Ben Ali", "a@b.tn", "Cardiologie", "short", "short").message());
        assertFalse(register("Amel Ben Ali", "a@b.tn", "Cardiologie", "x".repeat(129), "x".repeat(129)).success());
        assertFalse(register("Amel Ben Ali", "a@b.tn", "Cardiologie", " ".repeat(12), " ".repeat(12)).success());
        assertEquals("Les mots de passe ne correspondent pas.",
                register("Amel Ben Ali", "a@b.tn", "Cardiologie", GOOD_PASSWORD, GOOD_PASSWORD + "!").message());
    }
}
