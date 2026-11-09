package application.controller;

import application.model.Patient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PatientDialogController implements Initializable {
    @FXML private Label dialogTitle;
    @FXML private Label errorLabel;
    private boolean saved;
    @FXML private TextField txtNom, txtPrenom, txtTelephone, txtEmail, txtAdresse, txtAllergies;
    @FXML private DatePicker dpDateNaissance;
    @FXML private ComboBox<String> cbSexe, cbGroupeSanguin;

    @SuppressWarnings("unused")
    private Patient patient;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbSexe.getItems().addAll("Masculin", "Feminin");
        cbGroupeSanguin.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

        if (dialogTitle.getScene() != null) {
            dialogTitle.getScene().getWindow().setOnShown(e -> {
                dialogTitle.getScene().getRoot().setUserData(this);
            });
        }
    }

    public void setPatient(Patient p) {
        this.patient = p;
        txtNom.setText(p.getNom());
        txtPrenom.setText(p.getPrenom());
        if (p.getDateNaissance() != null && !p.getDateNaissance().isEmpty()) {
            dpDateNaissance.setValue(LocalDate.parse(p.getDateNaissance()));
        }
        cbSexe.setValue(p.getSexe());
        txtTelephone.setText(p.getTelephone());
        txtEmail.setText(p.getEmail());
        txtAdresse.setText(p.getAdresse());
        cbGroupeSanguin.setValue(p.getGroupeSanguin());
        txtAllergies.setText(p.getAllergies());
        dialogTitle.setText("Modifier Patient");
    }

    public Patient getPatient() {
        try { dpDateNaissance.commitValue(); }
        catch (RuntimeException e) { throw new IllegalArgumentException("La date de naissance est invalide."); }
        Patient p = new Patient();
        p.setNom(txtNom.getText());
        p.setPrenom(txtPrenom.getText());
        p.setDateNaissance(dpDateNaissance.getValue() != null ? dpDateNaissance.getValue().toString() : null);
        p.setSexe(cbSexe.getValue());
        p.setTelephone(txtTelephone.getText());
        p.setEmail(txtEmail.getText());
        p.setAdresse(txtAdresse.getText());
        p.setGroupeSanguin(cbGroupeSanguin.getValue());
        p.setAllergies(txtAllergies.getText());
        application.dao.PatientDAO.validatePatient(p);
        return p;
    }

    public boolean saveNewPatient() {
        if(saved)return true;
        try {
            new application.dao.PatientDAO().addPatient(getPatient());
            saved=true;return true;
        }catch(IllegalArgumentException e){showError(e.getMessage());}
        catch(java.sql.SQLException e){showError("Enregistrement impossible. Vos informations sont conservées. Réessayez ou vérifiez la configuration locale.");}
        return false;
    }
    private void showError(String message){errorLabel.setText(message);errorLabel.setVisible(true);errorLabel.setManaged(true);}
}
