package application.controller;

import application.model.Medecin;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MedecinDialogController {
    @FXML private Label dialogTitle;
    @FXML private TextField txtNom, txtPrenom, txtTelephone, txtEmail, txtEmailConnexion;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbSpecialite;
    @FXML private CheckBox chkDisponible;

    @SuppressWarnings("unused")
    private Medecin medecin;

    @FXML
    public void initialize() {
        cbSpecialite.getItems().addAll("Cardiologie", "Pediatrie", "Neurologie", "Dermatologie", 
            "Orthopedie", "Ophtalmologie", "Gynecologie", "Generaliste", "Chirurgie", "Radiologie");
    }

    public void setMedecin(Medecin m) {
        this.medecin = m;
        txtNom.setText(m.getNom());
        txtPrenom.setText(m.getPrenom());
        cbSpecialite.setValue(m.getSpecialite());
        txtTelephone.setText(m.getTelephone());
        txtEmail.setText(m.getEmail());
        txtEmailConnexion.setText(m.getEmailConnexion());
        chkDisponible.setSelected(m.isDisponible());
        dialogTitle.setText("Modifier Medecin");
    }

    public Medecin getMedecin() {
        if (txtNom.getText().isBlank() || txtPrenom.getText().isBlank() || cbSpecialite.getValue() == null
            || !txtEmailConnexion.getText().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
            || ((medecin == null || !txtPassword.getText().isEmpty()) && txtPassword.getText().length() < 12)) {
            throw new IllegalArgumentException("Nom, prénom, spécialité, email de connexion et mot de passe de 12 caractères requis pour un nouveau compte.");
        }
        Medecin m = new Medecin();
        m.setNom(txtNom.getText());
        m.setPrenom(txtPrenom.getText());
        m.setSpecialite(cbSpecialite.getValue());
        m.setTelephone(txtTelephone.getText());
        m.setEmail(txtEmail.getText());
        m.setEmailConnexion(txtEmailConnexion.getText().trim());
        m.setMotDePasse(txtPassword.getText());
        m.setDisponible(chkDisponible.isSelected());
        return m;
    }
}
