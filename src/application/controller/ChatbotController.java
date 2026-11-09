package application.controller;

import application.chatbot.HospitalChatbot;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ChatbotController {
    @FXML private TextArea chatArea;
    @FXML private TextField inputField;

    private HospitalChatbot chatbot = new HospitalChatbot();

    @FXML
    public void initialize() {
        appendMessage("MediBot", "Bienvenue ! Je suis MediBot, votre assistant hospitalier. Tapez 'aide' pour voir les commandes disponibles.");
    }

    @FXML
    private void handleSend() {
        String query = inputField.getText().trim();
        if (query.isEmpty()) return;

        appendMessage("Vous", query);
        String response = chatbot.processQuery(query);
        appendMessage("MediBot", response);

        inputField.clear();
    }

    @FXML
    private void quickRdv() { sendQuick("rdv du jour"); }

    @FXML
    private void quickMedecins() { sendQuick("liste des medecins"); }

    @FXML
    private void quickMeds() { sendQuick("medicaments"); }

    @FXML
    private void quickStats() { sendQuick("statistiques"); }

    @FXML
    private void quickHelp() { sendQuick("aide"); }

    private void sendQuick(String query) {
        appendMessage("Vous", query);
        String response = chatbot.processQuery(query);
        appendMessage("MediBot", response);
    }

    private void appendMessage(String sender, String message) {
        String prefix = sender.equals("Vous") ? "👤 Vous" : "🤖 MediBot";
        chatArea.appendText(prefix + " :\n" + message + "\n\n");
        chatArea.setScrollTop(Double.MAX_VALUE);
    }
}
