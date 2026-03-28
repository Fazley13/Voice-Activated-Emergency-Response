package com.codevengers.voiceemergency;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationUtil {
    
    public static void navigateToPage(Node sourceNode, String fxmlFile, String title) {
        try {
            Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlFile));
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            
            // Create new scene
            Scene newScene = new Scene(root);
            
            // Set scene and title
            stage.setScene(newScene);
            stage.setTitle(title);
            
            // Ensure window stays maximized
            stage.show();
            stage.setMaximized(true);
            
            // Force focus and bring to front
            stage.toFront();
            stage.requestFocus();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Navigation error to " + fxmlFile + ": " + e.getMessage());
        }
    }
}