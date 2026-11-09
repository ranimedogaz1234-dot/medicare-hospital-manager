package application.controller;

import application.App;
import application.service.AuthService;
import application.service.AuthService.LoginResult;
import application.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        SessionManager.clear();
        emailField.clear();
        passwordField.clear();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isBlank() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        try {
            LoginResult result = authService.login(email, password);
            switch (result.getType()) {
                case ADMIN -> {
                    SessionManager.setAdminSession(result.getAdmin());
                    App.changeScene("dashboard.fxml");
                }
                case MEDECIN -> {
                    SessionManager.setMedecinSession(result.getMedecin());
                    App.changeScene("medecin_dashboard.fxml");
                }
                case FAILED -> showError("Email ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            showError("Connexion indisponible. Vérifiez la configuration locale.");
        }
    }

    @FXML private void showRegistration() {
        passwordField.clear();
        App.changeScene("register_doctor.fxml");
    }

    @FXML
    private void closeApp() {
        System.exit(0);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(true);
    }
}

