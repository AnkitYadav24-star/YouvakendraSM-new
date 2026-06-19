import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            DashboardView root = new DashboardView();
            Scene scene = new Scene(root, 1100, 650);

            // Load custom styles
            java.io.File cssFile = new java.io.File("styles.css");
            if (cssFile.exists()) {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            } else if (getClass().getResource("styles.css") != null) {
                scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            } else {
                System.out.println("Warning: styles.css not found, running with default styling.");
            }

            primaryStage.setTitle("YouvakendraSM");

            // Set window icon
            try {
                java.io.File iconFile = new java.io.File("assets/logo.png");
                if (iconFile.exists()) {
                    primaryStage.getIcons()
                            .add(new javafx.scene.image.Image(iconFile.toURI().toURL().toExternalForm()));
                } else if (getClass().getResource("/assets/logo.png") != null) {
                    primaryStage.getIcons().add(
                            new javafx.scene.image.Image(getClass().getResource("/assets/logo.png").toExternalForm()));
                } else if (getClass().getResource("assets/logo.png") != null) {
                    primaryStage.getIcons().add(
                            new javafx.scene.image.Image(getClass().getResource("assets/logo.png").toExternalForm()));
                }
            } catch (Exception e) {
                System.out.println("Warning: Could not load application icon: " + e.getMessage());
            }

            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(650);
            primaryStage.show();

            // Run GitHub-based auto update check in the background
            VersionChecker.checkForUpdatesAsync(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
