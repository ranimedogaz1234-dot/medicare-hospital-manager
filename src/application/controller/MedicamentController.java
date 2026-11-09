package application.controller;

import application.model.Medicament;
import application.dao.MedicamentDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import java.sql.SQLException;

public class MedicamentController {
    @FXML private TableView<Medicament> tableMedicaments;
    @FXML private TableColumn<Medicament, Number> colId;
    @FXML private TableColumn<Medicament, String> colNom, colDosage, colIndications;
    @FXML private TableColumn<Medicament, Number> colStock;
    @FXML private TableColumn<Medicament, Number> colPrix;
    @FXML private TableColumn<Medicament, Void> colActions;
    @FXML private Label lblTotal;

    private MedicamentDAO medicamentDAO = new MedicamentDAO();
    private ObservableList<Medicament> medicaments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadMedicaments();
    }

    private void setupTable() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colNom.setCellValueFactory(cell -> cell.getValue().nomProperty());
        colDosage.setCellValueFactory(cell -> cell.getValue().dosageProperty());
        colIndications.setCellValueFactory(cell -> cell.getValue().indicationsProperty());
        colStock.setCellValueFactory(cell -> cell.getValue().stockProperty());
        colPrix.setCellValueFactory(cell -> cell.getValue().prixProperty());

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("action-button-edit");
                btnDelete.getStyleClass().add("action-button-danger");
                btnEdit.setOnAction(event -> editMedicament(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> deleteMedicament(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadMedicaments() {
        try {
            medicaments = medicamentDAO.getAllMedicaments();
            tableMedicaments.setItems(medicaments);
            lblTotal.setText("Total: " + medicaments.size() + " medicaments");
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les medicaments: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewMedicament() {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nouveau Medicament");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/medicament_dialog.fxml"));
            dialog.setDialogPane(loader.load());

            var cssUrl = getClass().getResource("/view/css/style.css");
            if (cssUrl != null) {
                dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }

            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                MedicamentDialogController controller = loader.getController();
                Medicament m = controller.getMedicament();
                medicamentDAO.addMedicament(m);
                loadMedicaments();
                showAlert("Succes", "Medicament ajoute avec succes!");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editMedicament(Medicament medicament) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier Medicament");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/medicament_dialog.fxml"));
            dialog.setDialogPane(loader.load());

            var cssUrl = getClass().getResource("/view/css/style.css");
            if (cssUrl != null) {
                dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }

            MedicamentDialogController controller = loader.getController();
            controller.setMedicament(medicament);

            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Medicament m = controller.getMedicament();
                m.setId(medicament.getId());
                medicamentDAO.updateMedicament(m);
                loadMedicaments();
                showAlert("Succes", "Medicament modifie avec succes!");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private void deleteMedicament(Medicament medicament) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le medicament");
        alert.setContentText("Etes-vous sur de vouloir supprimer " + medicament.getNom() + " ?");

        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                medicamentDAO.deleteMedicament(medicament.getId());
                loadMedicaments();
                showAlert("Succes", "Medicament supprime avec succes!");
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
