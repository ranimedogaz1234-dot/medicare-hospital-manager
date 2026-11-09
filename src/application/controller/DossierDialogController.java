package application.controller;

import application.model.DossierMedical;
import application.model.Patient;
import application.model.Medecin;
import application.dao.PatientDAO;
import application.dao.MedecinDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.time.LocalDate;

public class DossierDialogController {
    @FXML private Label dialogTitle;
    @FXML private ComboBox<Patient> cbPatient;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Medecin> cbMedecin;
    @FXML private TextArea txtDiagnostic, txtTraitement, txtNotes;

    @SuppressWarnings("unused")
    private DossierMedical dossier;
    private PatientDAO patientDAO = new PatientDAO();
    private MedecinDAO medecinDAO = new MedecinDAO();

    @FXML
    public void initialize() {
        dpDate.setValue(LocalDate.now());
    }

    public void loadData() {
        try {
            ObservableList<Patient> patients = patientDAO.getAllPatients();
            cbPatient.setItems(patients);
            cbPatient.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Patient item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getNomComplet());
                }
            });
            cbPatient.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Patient item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getNomComplet());
                }
            });

            ObservableList<Medecin> medecins = medecinDAO.getAllMedecins();
            cbMedecin.setItems(medecins);
            cbMedecin.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Medecin item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : "Dr. " + item.getNomComplet());
                }
            });
            cbMedecin.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Medecin item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : "Dr. " + item.getNomComplet());
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setDossier(DossierMedical dm) {
        this.dossier = dm;
        for (Patient p : cbPatient.getItems()) {
            if (p.getId() == dm.getPatientId()) {
                cbPatient.setValue(p);
                break;
            }
        }
        dpDate.setValue(LocalDate.parse(dm.getDateConsultation()));
        for (Medecin m : cbMedecin.getItems()) {
            if (m.getId() == dm.getMedecinId()) {
                cbMedecin.setValue(m);
                break;
            }
        }
        txtDiagnostic.setText(dm.getDiagnostic());
        txtTraitement.setText(dm.getTraitement());
        txtNotes.setText(dm.getNotes());
        dialogTitle.setText("Modifier Dossier Medical");
    }

    public DossierMedical getDossier() {
        DossierMedical dm = new DossierMedical();
        Patient p = cbPatient.getValue();
        Medecin m = cbMedecin.getValue();
        if (p != null) dm.setPatientId(p.getId());
        if (m != null) dm.setMedecinId(m.getId());
        dm.setDateConsultation(dpDate.getValue().toString());
        dm.setDiagnostic(txtDiagnostic.getText());
        dm.setTraitement(txtTraitement.getText());
        dm.setNotes(txtNotes.getText());
        return dm;
    }
}
