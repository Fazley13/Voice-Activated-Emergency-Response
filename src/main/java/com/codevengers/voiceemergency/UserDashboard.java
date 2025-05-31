package com.codevengers.voiceemergency;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UserDashboard extends Application {

    private String username;

    public UserDashboard(String username) {
        this.username = username;
    }

    public UserDashboard() {
        this.username = "User"; // default for testing
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("user_dashboard.fxml"));
        Parent root = loader.load();

        // Pass username to controller
        UserDashboardController controller = loader.getController();
        controller.setUsername(username);

        Scene scene = new Scene(root, 350, 320);
        stage.setTitle("User Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
