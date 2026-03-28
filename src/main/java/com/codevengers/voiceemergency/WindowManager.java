package com.codevengers.voiceemergency;

import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;

public class WindowManager {
    private static double screenWidth;
    private static double screenHeight;
    
    static {
        // Get screen dimensions once
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        screenWidth = screenBounds.getWidth();
        screenHeight = screenBounds.getHeight();
    }
    
    public static void maximizeWindow(Stage stage) {
        // Force window to full screen size
        stage.setX(0);
        stage.setY(0);
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);
        stage.setMaximized(true);
    }
    
    public static void setupMaximizedWindow(Stage stage) {
        // Set window properties before showing
        stage.setResizable(true);
        maximizeWindow(stage);
    }
}