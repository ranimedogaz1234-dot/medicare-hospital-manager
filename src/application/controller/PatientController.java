package application.controller;

import application.model.Patient;
import application.dao.PatientDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import java.sql.SQLException;

public class PatientController {
    @FXML private TableView<Patient> tablePatients;
    @FXML private TableColumn<Patient, Number> colId;
    @FXML private TableColumn<Patient, String> colNom, colPrenom, colDateNaissance, colSexe, colTelephone, colEmail, colGroupeSanguin;
    @FXML private TableColumn<Patient, Void> colActions;
    @FXML private TextField searchField;
    @FXML private Label lblTotal;

    private PatientDAO patientDAO = new PatientDAO();
    private ObservableList<Patient> patients = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadPatients();

        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) loadPatients();
            else searchPatients(newVal);
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colNom.setCellValueFactory(cell -> cell.getValue().nomProperty());
        colPrenom.setCellValueFactory(cell -> cell.getValue().prenomProperty());
        colDateNaissance.setCellValueFactory(cell -> cell.getValue().dateNaissanceProperty());
        colSexe.setCellValueFactory(cell -> cell.getValue().sexeProperty());
        colTelephone.setCellValueFactory(cell -> cell.getValue().telephoneProperty());
        colEmail.setCellValueFactory(cell -> cell.getValue().emailProperty());
        colGroupeSanguin.setCellValueFactory(cell -> cell.getValue().groupeSanguinProperty());

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("action-button-edit");
                btnDelete.getStyleClass().add("action-button-danger");
                btnEdit.setOnAction(event -> editPatient(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> deletePatient(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadPatients() {
        try {
            patients = patientDAO.getAllPatients();
            tablePatients.setItems(patients);
            lblTotal.setText("Total: " + patients.size() + " patients");
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les patients: " + e.getMessage());
        }
    }

    private void searchPatients(String keyword) {
        try {
            patients = patientDAO.searchPatients(keyword);
            tablePatients.setItems(patients);
            lblTotal.setText("Resultats: " + patients.size() + " patients");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de recherche: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewPatient() {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nouveau Patient");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/patient_dialog.fxml"));
            dialog.setDialogPane(loader.load());
            PatientDialogController controller = loader.getController();

            var cssUrl = getClass().getResource("/view/css/style.css");
            if (cssUrl != null) {
                dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }

            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Patient p = controller.getPatient();
                patientDAO.addPatient(p);
                loadPatients();
                showAlert("Succes", "Patient ajoute avec succes!");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void editPatient(Patient patient) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier Patient");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/patient_dialog.fxml"));
            dialog.setDialogPane(loader.load());

            var cssUrl = getClass().getResource("/view/css/style.css");
            if (cssUrl != null) {
                dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }

            PatientDialogController controller = loader.getController();
            controller.setPatient(patient);

            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Patient p = controller.getPatient();
                p.setId(patient.getId());
                patientDAO.updatePatient(p);
                loadPatients();
                showAlert("Succes", "Patient modifie avec succes!");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private void deletePatient(Patient patient) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le patient");
        alert.setContentText("Etes-vous sur de vouloir supprimer " + patient.getNomComplet() + " ?");

        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                patientDAO.deletePatient(patient.getId());
                loadPatients();
                showAlert("Succes", "Patient supprime avec succes!");
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

