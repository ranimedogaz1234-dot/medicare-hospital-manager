package application.controller;

import application.model.Medecin;
import application.dao.MedecinDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import java.sql.SQLException;

public class MedecinController {
    @FXML private TableView<Medecin> tableMedecins;
    @FXML private TableColumn<Medecin, Number> colId;
    @FXML private TableColumn<Medecin, String> colNom, colPrenom, colSpecialite, colTelephone, colEmail;
    @FXML private TableColumn<Medecin, Boolean> colDisponible;
    @FXML private TableColumn<Medecin, Void> colActions;
    @FXML private Label lblTotal;

    private MedecinDAO medecinDAO = new MedecinDAO();
    private ObservableList<Medecin> medecins = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadMedecins();
    }

    private void setupTable() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colNom.setCellValueFactory(cell -> cell.getValue().nomProperty());
        colPrenom.setCellValueFactory(cell -> cell.getValue().prenomProperty());
        colSpecialite.setCellValueFactory(cell -> cell.getValue().specialiteProperty());
        colTelephone.setCellValueFactory(cell -> cell.getValue().telephoneProperty());
        colEmail.setCellValueFactory(cell -> cell.getValue().emailProperty());
        colDisponible.setCellValueFactory(cell -> cell.getValue().disponibleProperty());
        colDisponible.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item ? "Oui" : "Non"));
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("action-button-edit");
                btnDelete.getStyleClass().add("action-button-danger");
                btnEdit.setOnAction(event -> editMedecin(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> deleteMedecin(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadMedecins() {
        try {
            medecins = medecinDAO.getAllMedecins();
            tableMedecins.setItems(medecins);
            lblTotal.setText("Total: " + medecins.size() + " medecins");
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les medecins: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewMedecin() {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nouveau Medecin");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/medecin_dialog.fxml"));
            dialog.setDialogPane(loader.load());

            var cssUrl = getClass().getResource("/view/css/style.css");
            if (cssUrl != null) {
                dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }

            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                MedecinDialogController controller = loader.getController();
                Medecin m = controller.getMedecin();
                medecinDAO.addMedecin(m);
                loadMedecins();
                showAlert("Succes", "Medecin ajoute avec succes!");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void editMedecin(Medecin medecin) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier Medecin");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/medecin_dialog.fxml"));
            dialog.setDialogPane(loader.load());

            var cssUrl = getClass().getResource("/view/css/style.css");
            if (cssUrl != null) {
                dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }

            MedecinDialogController controller = loader.getController();
            controller.setMedecin(medecin);

            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Medecin m = controller.getMedecin();
                m.setId(medecin.getId());
                if (m.getMotDePasse().isEmpty()) medecinDAO.updateMedecin(m);
                else medecinDAO.updateMedecinWithPassword(m);
                loadMedecins();
                showAlert("Succes", "Medecin modifie avec succes!");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private void deleteMedecin(Medecin medecin) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le medecin");
        alert.setContentText("Etes-vous sur de vouloir supprimer Dr. " + medecin.getNomComplet() + " ?");

        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                medecinDAO.deleteMedecin(medecin.getId());
                loadMedecins();
                showAlert("Succes", "Medecin supprime avec succes!");
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
