package application.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.model.RendezVous;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class AppointmentRulesTest {

    private static RendezVous appointment(LocalDateTime when, String status) {
        RendezVous r = new RendezVous();
        r.setPatientId(1);
        r.setMedecinId(2);
        r.setDateRdv(when.toLocalDate().toString());
        r.setHeure(when.format(DateTimeFormatter.ofPattern("HH:mm")));
        r.setStatut(status);
        r.setMotif("Contrôle");
        return r;
    }

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({
            "planifie, Planifié", "PLANIFIÉ, Planifié", "' Confirmé ', Confirmé", "en cours, En cours",
            "effectue, Terminé", "Terminé, Terminé", "annulé, Annulé", "inconnu, Autres", "'', Autres",
    })
    void statusesAreCanonicalisedIgnoringCaseAccentsAndSpaces(String raw, String expected) {
        assertEquals(expected, AppointmentRules.canonicalStatus(raw));
    }

    @Test
    void nullStatusIsOther() {
        assertEquals("Autres", AppointmentRules.canonicalStatus(null));
    }

    @Test
    void reportsGroupConfirmedAndInProgressWithPlanned() {
        assertEquals("Planifié", AppointmentRules.reportStatus("Confirmé"));
        assertEquals("Planifié", AppointmentRules.reportStatus("en cours"));
        assertEquals("Terminé", AppointmentRules.reportStatus("Terminé"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"24:00", "9:30", "12:60", "noon", ""})
    void invalidTimesAreRejected(String time) {
        RendezVous r = appointment(LocalDateTime.of(2030, 1, 15, 10, 0), "Planifié");
        r.setHeure(time);
        assertThrows(IllegalArgumentException.class, () -> AppointmentRules.start(r));
    }

    @Test
    void invalidDateIsRejected() {
        RendezVous r = appointment(LocalDateTime.of(2030, 1, 15, 10, 0), "Planifié");
        r.setDateRdv("2030-02-30");
        assertThrows(IllegalArgumentException.class, () -> AppointmentRules.start(r));
    }

    @Test
    void newAppointmentMustBeInTheFuture() {
        RendezVous past = appointment(AppointmentRules.now().minusDays(1), "Planifié");
        assertThrows(IllegalArgumentException.class, () -> AppointmentRules.validate(past, true, false));

        RendezVous future = appointment(AppointmentRules.now().plusDays(1), "planifie");
        assertDoesNotThrow(() -> AppointmentRules.validate(future, true, false));
        assertEquals("Planifié", future.getStatut(), "validate() stores the canonical status");
    }

    @Test
    void pastAppointmentCanBeClosedButNotKeptOpen() {
        RendezVous done = appointment(AppointmentRules.now().minusDays(1), "Terminé");
        assertDoesNotThrow(() -> AppointmentRules.validate(done, false, false));

        RendezVous stillPlanned = appointment(AppointmentRules.now().minusDays(1), "Planifié");
        assertThrows(IllegalArgumentException.class, () -> AppointmentRules.validate(stillPlanned, false, false));
    }

    @Test
    void requiresPatientDoctorAndKnownStatus() {
        RendezVous noPatient = appointment(AppointmentRules.now().plusDays(1), "Planifié");
        noPatient.setPatientId(0);
        assertThrows(IllegalArgumentException.class, () -> AppointmentRules.validate(noPatient, true, false));

        RendezVous badStatus = appointment(AppointmentRules.now().plusDays(1), "Reporté");
        assertThrows(IllegalArgumentException.class, () -> AppointmentRules.validate(badStatus, true, false));
    }

    @Test
    void rejectsOversizedOrNulByteReasons() {
        RendezVous longReason = appointment(AppointmentRules.now().plusDays(1), "Planifié");
        longReason.setMotif("x".repeat(2001));
        assertThrows(IllegalArgumentException.class, () -> AppointmentRules.validate(longReason, true, false));

        RendezVous nulReason = appointment(AppointmentRules.now().plusDays(1), "Planifié");
        nulReason.setMotif("a\0b");
        assertThrows(IllegalArgumentException.class, () -> AppointmentRules.validate(nulReason, true, false));
    }
}
