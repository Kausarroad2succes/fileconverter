package com.fileconverter.controller;

import com.fileconverter.model.ThemeState;
import com.fileconverter.service.RemoteConfigService;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController {

    @FXML
    private PasswordField codeField;

    @FXML
    private Button unlockButton;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox premiumPanel;

    @FXML
    private CheckBox nightModeToggle;

    private static final String DARK_MODE_CSS = "/com/fileconverter/fxml/css/dark-mode.css";

    @FXML
    public void initialize() {
        nightModeToggle.setSelected(ThemeState.isDarkModeEnabled());
    }

    @FXML
    private void handleUnlock() {
        String enteredCode = codeField.getText();
        if (enteredCode == null || enteredCode.isBlank()) {
            statusLabel.setText("Enter a code first");
            return;
        }

        Task<Boolean> verifyTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return RemoteConfigService.verifyPremiumCode(enteredCode);
            }
        };

        unlockButton.setDisable(true);
        loadingIndicator.setVisible(true);
        loadingIndicator.setManaged(true);
        statusLabel.setText("Checking code...");

        verifyTask.setOnSucceeded(e -> {
            unlockButton.setDisable(false);
            loadingIndicator.setVisible(false);
            loadingIndicator.setManaged(false);

            boolean isValid = verifyTask.getValue();
            if (isValid) {
                statusLabel.setText("");
                premiumPanel.setVisible(true);
                premiumPanel.setManaged(true);
            } else {
                statusLabel.setText("Invalid code");
                premiumPanel.setVisible(false);
                premiumPanel.setManaged(false);
            }
        });

        verifyTask.setOnFailed(e -> {
            unlockButton.setDisable(false);
            loadingIndicator.setVisible(false);
            loadingIndicator.setManaged(false);
            Throwable ex = verifyTask.getException();
            statusLabel.setText("Could not reach server: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        Thread thread = new Thread(verifyTask, "premium-verify-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleToggleNightMode() {
        Scene scene = nightModeToggle.getScene();
        String cssUrl = getClass().getResource(DARK_MODE_CSS).toExternalForm();

        boolean enabled = nightModeToggle.isSelected();
        ThemeState.setDarkModeEnabled(enabled);

        if (enabled) {
            if (!scene.getStylesheets().contains(cssUrl)) {
                scene.getStylesheets().add(cssUrl);
            }
        } else {
            scene.getStylesheets().remove(cssUrl);
        }
    }

    @FXML
    private void handleBackFromSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fileconverter/fxml/main.fxml"));
            Parent root = loader.load();

            Node source = (Node) codeField;
            Stage stage = (Stage) source.getScene().getWindow();

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            stage.setTitle("File converter");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}