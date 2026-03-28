package com.codevengers.voiceemergency;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoiceConfirmationPopup {

    private Stage popupStage;
    private Timeline countdownTimeline;
    private ScheduledExecutorService scheduler;
    private AtomicBoolean emergencyConfirmed = new AtomicBoolean(false);
    private AtomicBoolean popupClosed = new AtomicBoolean(false);

    // Configuration
    private static final int CONFIRMATION_TIMEOUT_SECONDS = 15;
    private static final String DETECTED_KEYWORD = "Emergency Detected";

    // Callback interface for emergency confirmation
    public interface EmergencyConfirmationCallback {
        void onEmergencyConfirmed(String detectedKeyword, String emergencyType);
        void onEmergencyDismissed(String reason);
    }

    private EmergencyConfirmationCallback callback;
    private String detectedKeyword;
    private String emergencyType;

    public VoiceConfirmationPopup(String detectedKeyword, String emergencyType, EmergencyConfirmationCallback callback) {
        this.detectedKeyword = detectedKeyword;
        this.emergencyType = emergencyType;
        this.callback = callback;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Show the confirmation popup
     */
    public void showConfirmationPopup() {
        Platform.runLater(() -> {
            try {
                createAndShowPopup();
                startCountdownTimer();
                scheduleAutoConfirmation();

                System.out.println("🚨 Voice confirmation popup displayed for: " + detectedKeyword);

            } catch (Exception e) {
                System.err.println("❌ Error showing confirmation popup: " + e.getMessage());
                e.printStackTrace();

                // Fallback: Auto-confirm emergency if popup fails
                if (callback != null) {
                    callback.onEmergencyConfirmed(detectedKeyword, emergencyType);
                }
            }
        });
    }

    /**
     * Create and display the popup window
     */
    private void createAndShowPopup() {
        popupStage = new Stage();
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.setTitle("🚨 Emergency Voice Detection");
        popupStage.setAlwaysOnTop(true);
        popupStage.setResizable(false);

        // Main container
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 10;");

        // Emergency icon and title
        Label emergencyIcon = new Label("🚨");
        emergencyIcon.setFont(Font.font("System", FontWeight.BOLD, 48));

        Label titleLabel = new Label("EMERGENCY VOICE DETECTED!");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-text-fill: #E74C3C;");

        // Detected keyword
        Label keywordLabel = new Label("Detected: \"" + detectedKeyword + "\"");
        keywordLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        keywordLabel.setTextFill(Color.WHITE);
        keywordLabel.setStyle("-fx-text-fill: #F39C12;");

        // Countdown label
        Label countdownLabel = new Label("Emergency will trigger in " + CONFIRMATION_TIMEOUT_SECONDS + " seconds");
        countdownLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        countdownLabel.setTextFill(Color.WHITE);
        countdownLabel.setId("countdownLabel"); // For timeline updates

        // Progress bar for visual countdown
        ProgressBar progressBar = new ProgressBar(1.0);
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #E74C3C;");
        progressBar.setId("progressBar"); // For timeline updates

        // Instructions
        Label instructionLabel = new Label("Double-click 'False Alarm' if this was NOT an emergency");
        instructionLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));
        instructionLabel.setTextFill(Color.WHITE);
        instructionLabel.setStyle("-fx-text-fill: #BDC3C7;");
        instructionLabel.setWrapText(true);

        // Buttons
        Button falseAlarmButton = createFalseAlarmButton();
        Button confirmEmergencyButton = createConfirmEmergencyButton();

        // Button container
        VBox buttonContainer = new VBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(falseAlarmButton, confirmEmergencyButton);

        // Add all components
        mainContainer.getChildren().addAll(
                emergencyIcon,
                titleLabel,
                keywordLabel,
                countdownLabel,
                progressBar,
                instructionLabel,
                buttonContainer
        );

        // Create scene
        Scene scene = new Scene(mainContainer, 400, 350);
        popupStage.setScene(scene);

        // Center on screen
        popupStage.centerOnScreen();

        // Handle window close
        popupStage.setOnCloseRequest(event -> {
            dismissEmergency("Window closed by user");
        });

        // Show popup
        popupStage.show();

        // Bring to front and request focus
        popupStage.toFront();
        popupStage.requestFocus();

        // Play alert sound
        playAlertSound();
    }

    /**
     * Create False Alarm button with double-click detection
     */
    private Button createFalseAlarmButton() {
        Button falseAlarmButton = new Button("🚫 FALSE ALARM - Not an Emergency");
        falseAlarmButton.setFont(Font.font("System", FontWeight.BOLD, 12));
        falseAlarmButton.setStyle(
                "-fx-background-color: #E74C3C; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 10 20 10 20;"
        );
        falseAlarmButton.setPrefWidth(300);

        // Double-click detection
        final long[] lastClickTime = {0};
        final int DOUBLE_CLICK_THRESHOLD = 500; // milliseconds

        falseAlarmButton.setOnMouseClicked(event -> {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastClickTime[0] < DOUBLE_CLICK_THRESHOLD) {
                // Double-click detected
                System.out.println("🚫 Double-click detected - Dismissing emergency as false alarm");
                dismissEmergency("Double-click on False Alarm button");
            } else {
                // Single click - show instruction
                falseAlarmButton.setText("🚫 DOUBLE-CLICK to confirm false alarm");
                falseAlarmButton.setStyle(
                        "-fx-background-color: #F39C12; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 5; " +
                                "-fx-padding: 10 20 10 20;"
                );

                // Reset button text after 2 seconds
                Timeline resetTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                    if (!popupClosed.get()) {
                        falseAlarmButton.setText("🚫 FALSE ALARM - Not an Emergency");
                        falseAlarmButton.setStyle(
                                "-fx-background-color: #E74C3C; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-background-radius: 5; " +
                                        "-fx-padding: 10 20 10 20;"
                        );
                    }
                }));
                resetTimeline.play();
            }

            lastClickTime[0] = currentTime;
        });

        return falseAlarmButton;
    }

    /**
     * Create Confirm Emergency button
     */
    private Button createConfirmEmergencyButton() {
        Button confirmButton = new Button("✅ YES - This IS an Emergency!");
        confirmButton.setFont(Font.font("System", FontWeight.BOLD, 12));
        confirmButton.setStyle(
                "-fx-background-color: #27AE60; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 10 20 10 20;"
        );
        confirmButton.setPrefWidth(300);

        confirmButton.setOnAction(event -> {
            System.out.println("✅ User confirmed emergency manually");
            confirmEmergency("User manually confirmed emergency");
        });

        return confirmButton;
    }

    /**
     * Start visual countdown timer
     */
    private void startCountdownTimer() {
        final int[] remainingSeconds = {CONFIRMATION_TIMEOUT_SECONDS};

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (popupClosed.get()) {
                return;
            }

            remainingSeconds[0]--;

            // Update countdown label
            Label countdownLabel = (Label) popupStage.getScene().lookup("#countdownLabel");
            if (countdownLabel != null) {
                if (remainingSeconds[0] > 0) {
                    countdownLabel.setText("Emergency will trigger in " + remainingSeconds[0] + " seconds");
                } else {
                    countdownLabel.setText("Triggering emergency now...");
                    countdownLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                }
            }

            // Update progress bar
            ProgressBar progressBar = (ProgressBar) popupStage.getScene().lookup("#progressBar");
            if (progressBar != null) {
                double progress = (double) remainingSeconds[0] / CONFIRMATION_TIMEOUT_SECONDS;
                progressBar.setProgress(progress);
            }

            // Change colors as time runs out
            if (remainingSeconds[0] <= 5) {
                if (countdownLabel != null) {
                    countdownLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                }
            }
        }));

        countdownTimeline.setCycleCount(CONFIRMATION_TIMEOUT_SECONDS);
        countdownTimeline.play();
    }

    /**
     * Schedule automatic emergency confirmation after timeout
     */
    private void scheduleAutoConfirmation() {
        scheduler.schedule(() -> {
            if (!popupClosed.get() && !emergencyConfirmed.get()) {
                Platform.runLater(() -> {
                    System.out.println("⏰ Timeout reached - Auto-confirming emergency");
                    confirmEmergency("Timeout - No user response");
                });
            }
        }, CONFIRMATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Confirm the emergency and trigger emergency procedures
     */
    private void confirmEmergency(String reason) {
        if (emergencyConfirmed.compareAndSet(false, true)) {
            System.out.println("🚨 Emergency CONFIRMED: " + reason);

            closePopup();

            if (callback != null) {
                callback.onEmergencyConfirmed(detectedKeyword, emergencyType);
            }
        }
    }

    /**
     * Dismiss the emergency as false alarm
     */
    private void dismissEmergency(String reason) {
        if (!emergencyConfirmed.get() && popupClosed.compareAndSet(false, true)) {
            System.out.println("🚫 Emergency DISMISSED: " + reason);

            closePopup();

            if (callback != null) {
                callback.onEmergencyDismissed(reason);
            }
        }
    }

    /**
     * Close the popup and cleanup resources
     */
    private void closePopup() {
        Platform.runLater(() -> {
            try {
                if (countdownTimeline != null) {
                    countdownTimeline.stop();
                }

                if (popupStage != null && popupStage.isShowing()) {
                    popupStage.close();
                }

                if (scheduler != null && !scheduler.isShutdown()) {
                    scheduler.shutdown();
                }

                System.out.println("✅ Voice confirmation popup closed and cleaned up");

            } catch (Exception e) {
                System.err.println("❌ Error closing popup: " + e.getMessage());
            }
        });
    }

    /**
     * Play alert sound to get user attention
     */
    private void playAlertSound() {
        try {
            // Use existing AlertSound class if available
            AlertSound.playAlert();
            System.out.println("🔊 Alert sound played for voice confirmation");
        } catch (Exception e) {
            System.err.println("❌ Could not play alert sound: " + e.getMessage());

            // Fallback: System beep
            try {
                java.awt.Toolkit.getDefaultToolkit().beep();
            } catch (Exception beepError) {
                System.err.println("❌ Could not play system beep: " + beepError.getMessage());
            }
        }
    }

    /**
     * Check if emergency was confirmed
     */
    public boolean isEmergencyConfirmed() {
        return emergencyConfirmed.get();
    }

    /**
     * Check if popup was closed
     */
    public boolean isPopupClosed() {
        return popupClosed.get();
    }

    /**
     * Force close the popup (for cleanup)
     */
    public void forceClose() {
        dismissEmergency("Force closed");
    }
}