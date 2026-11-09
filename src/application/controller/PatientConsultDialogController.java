package application.controller;

import application.dao.DossierMedicalDAO;
import application.model.DossierMedical;
import application.model.Patient;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;

public class PatientConsultDialogController {
    @FXML private Label lblNomComplet, lblDateNaissance, lblSexe, lblTelephone, lblEmail;
    @FXML private Label lblAdresse, lblGroupeSanguin, lblAllergies, lblDateInscription;
    @FXML private TableView<DossierMedical> tableDossiers;
    @FXML private TableColumn<DossierMedical, String> colDate, colDiagnostic, colTraitement;

    private final DossierMedicalDAO dossierDAO = new DossierMedicalDAO();

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(cell -> cell.getValue().dateConsultationProperty());
        colDiagnostic.setCellValueFactory(cell -> cell.getValue().diagnosticProperty());
        colTraitement.setCellValueFactory(cell -> cell.getValue().traitementProperty());
    }

    public void setPatient(Patient patient) throws SQLException {
        patient = new application.dao.PatientDAO().getPatientById(patient.getId());
        if (patient == null) throw new SQLException("Patient introuvable ou accès non autorisé.");
        lblNomComplet.setText(patient.getNomComplet());
        lblDateNaissance.setText(nullToDash(patient.getDateNaissance()));
        lblSexe.setText(nullToDash(patient.getSexe()));
        lblTelephone.setText(nullToDash(patient.getTelephone()));
        lblEmail.setText(nullToDash(patient.getEmail()));
        lblAdresse.setText(nullToDash(patient.getAdresse()));
        lblGroupeSanguin.setText(nullToDash(patient.getGroupeSanguin()));
        lblAllergies.setText(nullToDash(patient.getAllergies()));
        lblDateInscription.setText(nullToDash(patient.getDateInscription()));
        loadDossiersPatient(patient.getId());
    }

    private void loadDossiersPatient(int patientId) {
        try {
            ObservableList<DossierMedical> all = dossierDAO.getDossiersByPatient(patientId);
            tableDossiers.setItems(all);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
