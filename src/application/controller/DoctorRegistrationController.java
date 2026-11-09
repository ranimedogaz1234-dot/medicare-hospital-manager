package application.controller;

import application.App;
import application.service.DoctorRegistrationService;
import application.util.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public final class DoctorRegistrationController {
    @FXML private TextField fullNameField,emailField;
    @FXML private ComboBox<String> specialtyField;
    @FXML private PasswordField passwordField,confirmPasswordField;
    @FXML private Label messageLabel;
    @FXML private Button registerButton;
    @FXML private Hyperlink backLink;
    @FXML private VBox fieldsBox;

    @FXML private void initialize() {
        SessionManager.clear();
        specialtyField.getItems().addAll("Cardiologie","Pediatrie","Neurologie","Dermatologie","Orthopedie","Ophtalmologie","Gynecologie","Generaliste","Chirurgie","Radiologie");
    }

    @FXML private void register() {
        if(registerButton.isDisabled())return;
        String name=fullNameField.getText(),email=emailField.getText(),specialty=specialtyField.getEditor().getText(),password=passwordField.getText(),confirm=confirmPasswordField.getText();
        setBusy(true);messageLabel.setVisible(false);
        Task<DoctorRegistrationService.Result> task=new Task<>() {
            @Override protected DoctorRegistrationService.Result call(){return new DoctorRegistrationService().register(name,email,specialty,password,confirm);}
        };
        task.setOnSucceeded(e->{setBusy(false);var result=task.getValue();messageLabel.setText(result.message());messageLabel.getStyleClass().removeAll("success-label","error-label");messageLabel.getStyleClass().add(result.success()?"success-label":"error-label");messageLabel.setVisible(true);if(result.success()){passwordField.clear();confirmPasswordField.clear();registerButton.setDisable(true);fieldsBox.setDisable(true);}});
        task.setOnFailed(e->{setBusy(false);messageLabel.setText("Inscription indisponible. Réessayez ou contactez l'administrateur.");messageLabel.setVisible(true);});
        Thread thread=new Thread(task,"doctor-registration");thread.setDaemon(true);thread.start();
    }
    private void setBusy(boolean busy){registerButton.setDisable(busy);fieldsBox.setDisable(busy);backLink.setDisable(busy);registerButton.setText(busy?"Création en cours…":"Créer mon compte médecin");}
    @FXML private void backToLogin(){passwordField.clear();confirmPasswordField.clear();App.changeScene("login.fxml");}
}
