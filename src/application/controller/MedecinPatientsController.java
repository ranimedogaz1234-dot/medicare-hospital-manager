package application.controller;

import application.dao.PatientDAO;
import application.model.Patient;
import application.util.SessionManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.sql.SQLException;

public class MedecinPatientsController {
    @FXML private TableView<Patient> tablePatients;
    @FXML private TableColumn<Patient, Number> colId;
    @FXML private TableColumn<Patient, String> colNom, colPrenom, colTelephone, colGroupeSanguin;
    @FXML private TableColumn<Patient, Void> colActions;
    @FXML private Label lblTotal;
    @FXML private Label lblMessage;

    private final PatientDAO patientDAO = new PatientDAO();
    private ObservableList<Patient> patients;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colNom.setCellValueFactory(cell -> cell.getValue().nomProperty());
        colPrenom.setCellValueFactory(cell -> cell.getValue().prenomProperty());
        colTelephone.setCellValueFactory(cell -> cell.getValue().telephoneProperty());
        colGroupeSanguin.setCellValueFactory(cell -> cell.getValue().groupeSanguinProperty());
        setupActionsColumn();
        loadPatients();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnConsulter = new Button("Consulter");
            {
                btnConsulter.getStyleClass().add("action-button-primary");
                btnConsulter.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    consulterPatient(patient);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(btnConsulter);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadPatients() {
        try {
            int medecinId = SessionManager.getMedecinId();
            patients = patientDAO.getPatientsByMedecin(medecinId);
            tablePatients.setItems(patients);
            lblTotal.setText("Total: " + patients.size() + " patients");
        } catch (SQLException e) {
            showMessage("Impossible de charger les patients. Reconnectez-vous ou vérifiez la configuration locale.");
        }
    }

    @FXML private void handleNewPatient() {
        try {
            createNewPatientDialog().showAndWait();
        }catch(Exception e){showMessage("Impossible d'ouvrir le formulaire. Réessayez ou reconnectez-vous.");}
    }
    private Dialog<ButtonType> createNewPatientDialog() throws java.io.IOException {
            Dialog<ButtonType> dialog=new Dialog<>();
            dialog.setTitle("Nouveau Patient");
            FXMLLoader loader=new FXMLLoader(getClass().getResource("/view/patient_dialog.fxml"));
            dialog.setDialogPane(loader.load());
            PatientDialogController controller=loader.getController();
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());
            ButtonType save=dialog.getDialogPane().getButtonTypes().stream().filter(b->b.getButtonData()==ButtonBar.ButtonData.OK_DONE).findFirst().orElseThrow();
            dialog.getDialogPane().lookupButton(save).addEventFilter(javafx.event.ActionEvent.ACTION,event->{
                if(!controller.saveNewPatient())event.consume();
                else {loadPatients();showMessage("Patient ajouté à votre liste.");}
            });
            return dialog;
    }
    private void showMessage(String text){lblMessage.setText(text);lblMessage.setManaged(true);lblMessage.setVisible(true);}

    private void consulterPatient(Patient patient) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setTitle("Informations patient");
            dialog.setHeaderText("Dossier de " + patient.getNomComplet());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/patient_consult_dialog.fxml"));
            DialogPane pane = loader.load();
            PatientConsultDialogController controller = loader.getController();
            controller.setPatient(patient);
            dialog.setDialogPane(pane);
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
