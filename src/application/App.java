package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import application.util.DatabaseUtil;

public class App extends Application {

    private static Stage primaryStage;
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) throws Exception {
        // Application-local locale; Windows settings are not changed.
        java.util.Locale.setDefault(java.util.Locale.FRENCH);
        primaryStage = stage;

        // Database setup/migrations must be explicitly enabled by the operator.
        if ("true".equalsIgnoreCase(System.getenv("HOSPITAL_DB_INITIALIZE"))) {
            try {
                DatabaseUtil.initializeDatabase();
            } catch (Exception e) {
                System.err.println("Database initialization failed: " + e.getMessage());
            }
        }


        // Charger le login
        // Using source folder FXMLs is not enough; FXML must be resolvable from the runtime classpath.
        // If your build copies resources to /bin, ensure they keep the same paths.
        var loginUrl = getClass().getResource("/view/login.fxml");
        if (loginUrl == null) {
            throw new IllegalStateException("FXML not found: /view/login.fxml (check resources folder + classpath copy to runtime)");
        }

        FXMLLoader loader = new FXMLLoader(loginUrl);
        Parent root = loader.load();


        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());

        // Rendre la fenêtre déplaçable
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.setTitle("MediCare HMS - Système de Gestion Hospitalière");
        stage.show();
    }

    public static void changeScene(String fxml) {
        if (!application.util.SessionManager.canOpenView(fxml)) {
            throw new SecurityException("Accès non autorisé à cette interface.");
        }
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/view/" + fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("/view/css/style.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Navigation indisponible. Vérifiez la session et la configuration locale.");
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

