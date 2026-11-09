package application.controller;

import application.model.RendezVous;
import application.model.Patient;
import application.model.Medecin;
import application.dao.PatientDAO;
import application.dao.MedecinDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("unused")
public class RdvDialogController {
    @FXML private Label dialogTitle;
    @FXML private ComboBox<Patient> cbPatient;
    @FXML private ComboBox<Medecin> cbMedecin;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbHeure;
    @FXML private TextField txtMotif;
    @FXML private ComboBox<String> cbStatut;

    private RendezVous rendezVous;
    private PatientDAO patientDAO = new PatientDAO();
    private MedecinDAO medecinDAO = new MedecinDAO();

    @FXML
    public void initialize() {
        cbStatut.getItems().addAll("Planifie", "Confirme", "En cours", "Termine", "Annule");
        cbStatut.setValue("Planifie");

        // Heures de 08:00 a 18:00 par tranche de 30min
        for (int h = 8; h <= 18; h++) {
            cbHeure.getItems().add(String.format("%02d:00", h));
            if (h < 18) cbHeure.getItems().add(String.format("%02d:30", h));
        }
        cbHeure.setValue("09:00");
        dpDate.setValue(LocalDate.now().plusDays(1));
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
                    setText(empty ? null : "Dr. " + item.getNomComplet() + " (" + item.getSpecialite() + ")");
                }
            });
            cbMedecin.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Medecin item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : "Dr. " + item.getNomComplet() + " (" + item.getSpecialite() + ")");
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRendezVous(RendezVous rdv) {
        this.rendezVous = rdv;
        // Selectionner le patient et medecin correspondants
        for (Patient p : cbPatient.getItems()) {
            if (p.getId() == rdv.getPatientId()) {
                cbPatient.setValue(p);
                break;
            }
        }
        for (Medecin m : cbMedecin.getItems()) {
            if (m.getId() == rdv.getMedecinId()) {
                cbMedecin.setValue(m);
                break;
            }
        }
        dpDate.setValue(LocalDate.parse(rdv.getDateRdv()));
        cbHeure.setValue(rdv.getHeure());
        txtMotif.setText(rdv.getMotif());
        cbStatut.setValue(rdv.getStatut());
        dialogTitle.setText("Modifier Rendez-vous");
    }

    public RendezVous getRendezVous() {
        RendezVous rdv = new RendezVous();
        Patient p = cbPatient.getValue();
        Medecin m = cbMedecin.getValue();
        if (p != null) rdv.setPatientId(p.getId());
        if (m != null) rdv.setMedecinId(m.getId());
        rdv.setDateRdv(dpDate.getValue().toString());
        rdv.setHeure(cbHeure.getValue());
        rdv.setMotif(txtMotif.getText());
        rdv.setStatut(cbStatut.getValue());
        return rdv;
    }
}
