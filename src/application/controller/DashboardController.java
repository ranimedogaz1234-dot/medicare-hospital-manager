package application.controller;

import application.App;
import application.dao.*;
import application.util.SessionManager;
import application.model.RendezVous;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardController {
    @FXML private StackPane contentPane;
    @FXML private VBox dashboardView;
    @FXML private Label lblTotalPatients, lblTotalMedecins, lblRdvToday, lblTotalMedicaments;
    @FXML private TableView<RendezVous> tableRdvToday;
    @FXML private TableColumn<RendezVous, String> colRdvHeure, colRdvPatient, colRdvMedecin, colRdvMotif;
    @FXML private ListView<String> listActivity;
    @FXML private Button btnDashboard, btnPatients, btnMedecins, btnRendezVous, btnDossiers, btnMedicaments, btnChatbot;

    private PatientDAO patientDAO = new PatientDAO();
    private MedecinDAO medecinDAO = new MedecinDAO();
    private RendezVousDAO rendezVousDAO = new RendezVousDAO();
    private MedicamentDAO medicamentDAO = new MedicamentDAO();

    @FXML
    public void initialize() throws SQLException {
        SessionManager.requireAdmin();
        loadStatistics();
        setupRdvTable();
        loadRdvToday();
        loadActivity();
    }

    private void loadStatistics() {
        try {
            lblTotalPatients.setText(String.valueOf(patientDAO.getTotalPatients()));
            lblTotalMedecins.setText(String.valueOf(medecinDAO.getTotalMedecins()));
            lblRdvToday.setText(String.valueOf(rendezVousDAO.getRendezVousToday()));
            lblTotalMedicaments.setText(String.valueOf(medicamentDAO.getTotalMedicaments()));
        } catch (SQLException e) {
            System.err.println("Section indisponible. Vérifiez la session et la configuration locale.");
        }
    }

    private void setupRdvTable() {
        colRdvHeure.setCellValueFactory(cell -> cell.getValue().heureProperty());
        colRdvPatient.setCellValueFactory(cell -> cell.getValue().patientNomProperty());
        colRdvMedecin.setCellValueFactory(cell -> cell.getValue().medecinNomProperty());
        colRdvMotif.setCellValueFactory(cell -> cell.getValue().motifProperty());
    }

    private void loadRdvToday() {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            ObservableList<RendezVous> rdvs = rendezVousDAO.getRendezVousByDate(today);
            tableRdvToday.setItems(rdvs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadActivity() {
        listActivity.getItems().setAll("Statistiques issues de la base locale", "Patients : " + lblTotalPatients.getText(), "Médecins : " + lblTotalMedecins.getText(), "Rendez-vous du jour : " + lblRdvToday.getText());
    }

    @FXML
    private void showDashboard() {
        setActiveButton(btnDashboard);
        contentPane.getChildren().clear();
        contentPane.getChildren().add(dashboardView);
        loadStatistics();
        loadRdvToday();
    }

    @FXML
    private void showPatients() { loadView("patients.fxml", btnPatients); }

    @FXML
    private void showMedecins() { loadView("medecins.fxml", btnMedecins); }

    @FXML
    private void showRendezVous() { loadView("rendezvous.fxml", btnRendezVous); }

    @FXML
    private void showDossiers() { loadView("dossiers.fxml", btnDossiers); }

    @FXML
    private void showMedicaments() { loadView("medicaments.fxml", btnMedicaments); }

    @FXML
    private void showChatbot() { loadView("chatbot.fxml", btnChatbot); }

    private void loadView(String fxml, Button activeBtn) {
        try {
            if (contentPane == null) {
                throw new IllegalStateException("contentPane est null : vérifier le fx:id=\"contentPane\" dans dashboard.fxml");
            }

            setActiveButton(activeBtn);

            var url = getClass().getResource("/view/" + fxml);
            if (url == null) {
                throw new IllegalStateException("FXML introuvable: /view/" + fxml + " (check resources classpath)");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button active) {
        btnDashboard.getStyleClass().removeAll("sidebar-button-active");
        btnDashboard.getStyleClass().add("sidebar-button");
        btnPatients.getStyleClass().removeAll("sidebar-button-active");
        btnPatients.getStyleClass().add("sidebar-button");
        btnMedecins.getStyleClass().removeAll("sidebar-button-active");
        btnMedecins.getStyleClass().add("sidebar-button");
        btnRendezVous.getStyleClass().removeAll("sidebar-button-active");
        btnRendezVous.getStyleClass().add("sidebar-button");
        btnDossiers.getStyleClass().removeAll("sidebar-button-active");
        btnDossiers.getStyleClass().add("sidebar-button");
        btnMedicaments.getStyleClass().removeAll("sidebar-button-active");
        btnMedicaments.getStyleClass().add("sidebar-button");
        btnChatbot.getStyleClass().removeAll("sidebar-button-active");
        btnChatbot.getStyleClass().add("sidebar-button");

        active.getStyleClass().removeAll("sidebar-button");
        active.getStyleClass().add("sidebar-button-active");
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        App.changeScene("login.fxml");
    }
}
