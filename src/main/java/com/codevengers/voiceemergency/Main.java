package com.codevengers.voiceemergency;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        TabPane tabPane = new TabPane();

        // Load each FXML as a tab content
        tabPane.getTabs().add(createTab("User Login", "UserLogin.fxml"));
        tabPane.getTabs().add(createTab("User Register", "UserRegister.fxml"));
        tabPane.getTabs().add(createTab("Admin Login", "AdminLogin.fxml"));
        tabPane.getTabs().add(createTab("Admin Register", "AdminRegister.fxml"));

        Scene scene = new Scene(tabPane, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Emergency Response - Login/Register");
        primaryStage.show();
    }

    private Tab createTab(String title, String fxmlFile) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent content = loader.load();
        Tab tab = new Tab(title);
        tab.setContent(content);
        return tab;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
