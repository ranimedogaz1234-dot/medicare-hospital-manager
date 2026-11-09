package application.controller;

import application.dao.DossierMedicalDAO;
import application.dao.PatientDAO;
import application.model.DossierMedical;
import application.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.sql.SQLException;

public class DossierController {
    @FXML private TableView<DossierMedical> tableDossiers;
    @FXML private TableColumn<DossierMedical, Number> colId;
    @FXML private TableColumn<DossierMedical, String> colPatient, colDateConsult, colDiagnostic, colTraitement, colMedecin;
    @FXML private TableColumn<DossierMedical, Void> colActions;
    @FXML private TextField searchField;
    @FXML private Label lblTotal;

    private final DossierMedicalDAO dossierDAO = new DossierMedicalDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    private ObservableList<DossierMedical> dossiers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadDossiers();

        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                loadDossiers();
            } else {
                searchByPatient(newVal);
            }
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colPatient.setCellValueFactory(cell -> cell.getValue().patientNomProperty());
        colDateConsult.setCellValueFactory(cell -> cell.getValue().dateConsultationProperty());
        colDiagnostic.setCellValueFactory(cell -> cell.getValue().diagnosticProperty());
        colTraitement.setCellValueFactory(cell -> cell.getValue().traitementProperty());

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("action-button-edit");
                btnDelete.getStyleClass().add("action-button-danger");
                btnEdit.setOnAction(event -> editDossier(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> deleteDossier(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadDossiers() {
        try {
            dossiers = dossierDAO.getAllDossiers();
            tableDossiers.setItems(dossiers);
            lblTotal.setText("Total: " + dossiers.size() + " dossiers");
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les dossiers: " + e.getMessage());
        }
    }

    private void searchByPatient(String keyword) {
        try {
            ObservableList<Patient> patients = patientDAO.searchPatients(keyword);
            if (!patients.isEmpty()) {
                dossiers = dossierDAO.getDossiersByPatient(patients.get(0).getId());
                tableDossiers.setItems(dossiers);
                lblTotal.setText("Resultats: " + dossiers.size() + " dossiers");
            } else {
                tableDossiers.setItems(FXCollections.observableArrayList());
                lblTotal.setText("Resultats: 0 dossiers");
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de recherche: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewDossier() {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nouveau Dossier Medical");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dossier_dialog.fxml"));
            DialogPane dialogPane = loader.load();
            dialog.setDialogPane(dialogPane);

            if (getClass().getResource("/view/css/style.css") != null) {
                dialogPane.getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());
            }

            DossierDialogController controller = loader.getController();
            if (controller != null) {
                controller.loadData();
            }

            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (controller == null) return;
                DossierMedical dm = controller.getDossier();
                dossierDAO.addDossier(dm);
                loadDossiers();
                showAlert("Succes", "Dossier medical ajoute avec succes!");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void editDossier(DossierMedical dossier) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier Dossier Medical");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dossier_dialog.fxml"));
            DialogPane dialogPane = loader.load();
            dialog.setDialogPane(dialogPane);

            if (getClass().getResource("/view/css/style.css") != null) {
                dialogPane.getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());
            }

            DossierDialogController controller = loader.getController();
            if (controller != null) {
                controller.loadData();
                controller.setDossier(dossier);
            }

            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (controller == null) return;
                DossierMedical dm = controller.getDossier();
                dm.setId(dossier.getId());
                dossierDAO.updateDossier(dm);
                loadDossiers();
                showAlert("Succes", "Dossier medical modifie avec succes!");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private void deleteDossier(DossierMedical dossier) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le dossier");
        alert.setContentText("Etes-vous sur de vouloir supprimer ce dossier medical ?");

        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dossierDAO.deleteDossier(dossier.getId());
                loadDossiers();
                showAlert("Succes", "Dossier medical supprime avec succes!");
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

