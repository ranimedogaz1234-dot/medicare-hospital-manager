package application.controller;

import application.App;
import application.model.Medecin;
import application.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MedecinDashboardController {
    @FXML private StackPane contentPane;
    @FXML private Label lblMedecinNom;
    @FXML private Label lblSpecialite;
    @FXML private Button btnAccueil, btnRendezVous, btnDossiers, btnPatients, btnProfil, btnChatbot;


    @FXML
    public void initialize() throws java.sql.SQLException {
        application.util.DatabaseUtil.verifyCurrentSession();
        if (!SessionManager.isMedecinLoggedIn()) throw new java.sql.SQLException("Compte médecin requis.");
        Medecin medecin = SessionManager.getCurrentMedecin();
        if (medecin != null) {
            lblMedecinNom.setText("Dr. " + medecin.getNomComplet());
            lblSpecialite.setText(medecin.getSpecialite());
        }
        showAccueil();
    }

    @FXML
    private void showAccueil() { loadView("medecin_accueil.fxml", btnAccueil); }

    @FXML
    private void showRendezVous() { loadView("medecin_rendezvous.fxml", btnRendezVous); }

    @FXML
    private void showDossiers() { loadView("medecin_dossiers.fxml", btnDossiers); }

    @FXML
    private void showPatients() { loadView("medecin_patients.fxml", btnPatients); }

    @FXML
    private void showProfil() { loadView("medecin_profil.fxml", btnProfil); }

    @FXML
    private void showChatbot() { loadView("chatbot.fxml", btnChatbot); }


    @FXML
    private void handleLogout() {
        SessionManager.clear();
        App.changeScene("login.fxml");
    }

    private void loadView(String fxml, Button activeBtn) {
        try {
            setActiveButton(activeBtn);

            // Important: utiliser le même chemin que le reste de l’app pour retrouver les FXML dans le classpath.
            var url = App.class.getResource("/view/" + fxml);
            if (url == null) {
                throw new IllegalStateException("FXML introuvable (check classpath/resources): /view/" + fxml);
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (Exception e) {

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Section indisponible");
            alert.setHeaderText("Impossible d’ouvrir cette section");
            alert.setContentText("Reconnectez-vous ou vérifiez la configuration locale.");
            alert.showAndWait();
        }
    }



    private void setActiveButton(Button active) {
        resetButton(btnAccueil);
        resetButton(btnRendezVous);
        resetButton(btnDossiers);
        resetButton(btnPatients);
        resetButton(btnProfil);
        resetButton(btnChatbot);
        active.getStyleClass().removeAll("sidebar-button");
        active.getStyleClass().add("sidebar-button-active");
    }


    private void resetButton(Button btn) {
        btn.getStyleClass().removeAll("sidebar-button-active");
        btn.getStyleClass().add("sidebar-button");
    }
}
