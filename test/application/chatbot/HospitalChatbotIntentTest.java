package application.chatbot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import application.chatbot.HospitalChatbot.Intent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Command routing of MediBot. These tests need no database: they only exercise intent detection. */
class HospitalChatbotIntentTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(delimiter = '|', value = {
            "Bonjour                         | GREETING",
            "hi                              | GREETING",
            "Salut !                         | GREETING",
            "RDV du jour                     | TODAY_APPOINTMENTS",
            "consultations aujourd'hui       | TODAY_APPOINTMENTS",
            "chercher patient Sami           | PATIENT_SEARCH",
            "Liste des médecins              | DOCTORS",
            "Médicaments                     | MEDICATIONS",
            "stock pharmacie                 | MEDICATIONS",
            "ajouter rdv                     | ADD_APPOINTMENT",
            "Résumer dossier Sami            | RECORD_SUMMARY",
            "Statistiques                    | STATISTICS",
            "Aide                            | HELP",
            "quelle heure est-il             | UNKNOWN",
    })
    void documentedCommands(String message, Intent expected) {
        assertEquals(expected, HospitalChatbot.detectIntent(message));
    }

    /** "hi" used to match inside these words, so they returned the greeting instead of the command. */
    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(delimiter = '|', value = {
            "historique patient Sami | RECORD_SUMMARY",
            "chiffres                | STATISTICS",
    })
    void wordsContainingHiAreNotGreetings(String message, Intent expected) {
        assertEquals(expected, HospitalChatbot.detectIntent(message));
    }

    /** The generic "patient" keyword used to win over these more specific commands. */
    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(delimiter = '|', value = {
            "historique patient Sami             | RECORD_SUMMARY",
            "dossier medical du patient Sami     | RECORD_SUMMARY",
            "nouveau rendez-vous pour un patient | ADD_APPOINTMENT",
    })
    void specificCommandsWinOverPatientSearch(String message, Intent expected) {
        assertEquals(expected, HospitalChatbot.detectIntent(message));
    }

    @Test
    void aGreetingFollowedByACommandRunsTheCommand() {
        assertEquals(Intent.MEDICATIONS, HospitalChatbot.detectIntent("Bonjour, médicaments en stock ?"));
    }

    @Test
    void nullAndBlankMessagesAreNotUnderstood() {
        assertEquals(Intent.UNKNOWN, HospitalChatbot.detectIntent(null));
        assertEquals(Intent.UNKNOWN, HospitalChatbot.detectIntent("   "));
    }

    @Test
    void extractNameSkipsCommandWordsWithOrWithoutAccents() {
        assertEquals("Sami", HospitalChatbot.extractName("chercher patient Sami"));
        assertEquals("Sami", HospitalChatbot.extractName("Résumer dossier Sami"));
        assertEquals("Sami", HospitalChatbot.extractName("dossier médical de Sami"));
        assertEquals("", HospitalChatbot.extractName("historique patient"));
    }
}
