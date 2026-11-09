package application.controller;

import application.App;
import application.dao.MedecinDAO;
import application.model.Medecin;
import application.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.sql.SQLException;

public class MedecinProfilController {
    @FXML private TextField txtNom, txtPrenom, txtTelephone, txtEmail, txtEmailConnexion;
    @FXML private ComboBox<String> cbSpecialite;
    @FXML private CheckBox chkDisponible;
    @FXML private PasswordField txtNewPassword, txtConfirmPassword;
    @FXML private Label lblMessage;

    private final MedecinDAO medecinDAO = new MedecinDAO();
    private Medecin medecin;


    @FXML
    public void initialize() {
        cbSpecialite.getItems().addAll(
            "Cardiologie", "Pediatrie", "Neurologie", "Dermatologie",
            "Orthopedie", "Ophtalmologie", "Gynecologie", "Generaliste", "Chirurgie", "Radiologie"
        );
        loadProfil();
    }

    private void loadProfil() {
        medecin = SessionManager.getCurrentMedecin();
        if (medecin == null) return;

        txtNom.setText(medecin.getNom());
        txtPrenom.setText(medecin.getPrenom());
        cbSpecialite.setValue(medecin.getSpecialite());
        txtTelephone.setText(medecin.getTelephone());
        txtEmail.setText(medecin.getEmail());
        txtEmailConnexion.setText(medecin.getEmailConnexion());
        chkDisponible.setSelected(medecin.isDisponible());
    }

    @FXML
    private void handleSaveProfil() {
        if (txtNom.getText().isBlank() || txtPrenom.getText().isBlank() || cbSpecialite.getValue() == null) {
            showMessage("Veuillez remplir les champs obligatoires", true);
            return;
        }
        if (txtEmailConnexion.getText().isBlank()) {
            showMessage("L'email de connexion est obligatoire", true);
            return;
        }

        try {
            medecin.setNom(txtNom.getText().trim());
            medecin.setPrenom(txtPrenom.getText().trim());
            medecin.setSpecialite(cbSpecialite.getValue());
            medecin.setTelephone(txtTelephone.getText().trim());
            medecin.setEmail(txtEmail.getText().trim());
            medecin.setEmailConnexion(txtEmailConnexion.getText().trim());
            medecin.setDisponible(chkDisponible.isSelected());
            medecinDAO.updateMedecin(medecin);
            SessionManager.setMedecinSession(medecin);
            showMessage("Profil mis a jour avec succes", false);
        } catch (SQLException e) {
            showMessage("Modification impossible. Vérifiez les informations ou reconnectez-vous.", true);
        }
    }

    @FXML
    private void handleChangePassword() {
        String pwd = txtNewPassword.getText();
        String confirm = txtConfirmPassword.getText();
        if (pwd.length() < 12 || pwd.length() > 1024) {
            showMessage("Utilisez un mot de passe de 12 à 1024 caractères", true);
            return;
        }
        if (pwd.isBlank()) {
            showMessage("Entrez un nouveau mot de passe", true);
            return;
        }
        if (!pwd.equals(confirm)) {
            showMessage("Les mots de passe ne correspondent pas", true);
            return;
        }
        try {
            medecinDAO.updatePassword(medecin.getId(), pwd);
            txtNewPassword.clear();
            txtConfirmPassword.clear();
            showMessage("Mot de passe modifie avec succes", false);
        } catch (SQLException e) {
            showMessage("Modification impossible. Vérifiez les informations ou reconnectez-vous.", true);
        }
    }

    @FXML
    private void handleDeleteProfil() {
        medecin = SessionManager.getCurrentMedecin();
        if (medecin == null) {
            showMessage("Aucun profil medecin en session", true);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer le profil");
        alert.setHeaderText("Confirmer la suppression");
        alert.setContentText("Etes-vous sur de vouloir supprimer votre compte (profil) ?\n\nCette action est irreversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;
            try {
                medecinDAO.deleteMedecin(medecin.getId());
                SessionManager.clear();
                App.changeScene("login.fxml");
            } catch (SQLException e) {
                showMessage("Modification impossible. Vérifiez les informations ou reconnectez-vous.", true);
            }
        });
    }


    private void showMessage(String message, boolean error) {
        lblMessage.setText(message);
        lblMessage.setTextFill(error ? Color.RED : Color.web("#27ae60"));
        lblMessage.setVisible(true);
    }
}
