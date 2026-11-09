package application.controller;

import application.model.RendezVous;
import application.model.Patient;
import application.model.Medecin;
import application.dao.RendezVousDAO;
import application.dao.PatientDAO;
import application.dao.MedecinDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("unused")
public class RendezVousController {
    @FXML private TableView<RendezVous> tableRendezVous;
    @FXML private TableColumn<RendezVous, Number> colId;
    @FXML private TableColumn<RendezVous, String> colPatient, colMedecin, colDate, colHeure, colMotif, colStatut;
    @FXML private TableColumn<RendezVous, Void> colActions;
    @FXML private DatePicker dateFilter;
    @FXML private Label lblTotal;

    private RendezVousDAO rendezVousDAO = new RendezVousDAO();
    private ObservableList<RendezVous> rendezVous = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadRendezVous();

        dateFilter.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                filterByDate(newVal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                loadRendezVous();
            }
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colPatient.setCellValueFactory(cell -> cell.getValue().patientNomProperty());
        colMedecin.setCellValueFactory(cell -> cell.getValue().medecinNomProperty());
        colDate.setCellValueFactory(cell -> cell.getValue().dateRdvProperty());
        colHeure.setCellValueFactory(cell -> cell.getValue().heureProperty());
        colMotif.setCellValueFactory(cell -> cell.getValue().motifProperty());
        colStatut.setCellValueFactory(cell -> cell.getValue().statutProperty());

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("action-button-edit");
                btnDelete.getStyleClass().add("action-button-danger");
                btnEdit.setOnAction(event -> {
                    RendezVous current = getTableRow().getItem();
                    if (current != null) {
                        editRdv(current);
                    }
                });
                btnDelete.setOnAction(event -> {
                    RendezVous current = getTableRow().getItem();
                    if (current != null) {
                        deleteRdv(current);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadRendezVous() {
        try {
            rendezVous = rendezVousDAO.getAllRendezVous();
            tableRendezVous.setItems(rendezVous);
            lblTotal.setText("Total: " + rendezVous.size() + " rendez-vous");
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les rendez-vous: " + e.getMessage());
        }
    }

    private void filterByDate(String date) {
        try {
            rendezVous = rendezVousDAO.getRendezVousByDate(date);
            tableRendezVous.setItems(rendezVous);
            lblTotal.setText("Resultats: " + rendezVous.size() + " rendez-vous");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de filtrage: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewRdv() {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nouveau Rendez-vous");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/rdv_dialog.fxml"));
            dialog.setDialogPane(loader.load());
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());


            RdvDialogController controller = loader.getController();
            controller.loadData();

            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                RendezVous rdv = controller.getRendezVous();
                rendezVousDAO.addRendezVous(rdv);
                loadRendezVous();
                showAlert("Succes", "Rendez-vous ajoute avec succes!");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void editRdv(RendezVous rdv) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier Rendez-vous");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/rdv_dialog.fxml"));
            dialog.setDialogPane(loader.load());
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());


            RdvDialogController controller = loader.getController();
            controller.loadData();
            controller.setRendezVous(rdv);

            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                RendezVous r = controller.getRendezVous();
                r.setId(rdv.getId());
                rendezVousDAO.updateRendezVous(r);
                loadRendezVous();
                showAlert("Succes", "Rendez-vous modifie avec succes!");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private void deleteRdv(RendezVous rdv) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Annuler le rendez-vous");
        alert.setContentText("Etes-vous sur de vouloir annuler ce rendez-vous ?");

        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                rendezVousDAO.deleteRendezVous(rdv.getId());
                loadRendezVous();
                showAlert("Succes", "Rendez-vous annule avec succes!");
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de l'annulation: " + e.getMessage());
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
