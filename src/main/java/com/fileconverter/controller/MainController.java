package com.fileconverter.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private void showPdfTools() {
        contentArea.getChildren().setAll(new Label("PDF Tools screen coming soon"));
    }

    @FXML
    private void showImageTools() {
        contentArea.getChildren().setAll(new Label("Image Tools screen coming soon"));
    }

    @FXML
    private void showAudioTools() {
        contentArea.getChildren().setAll(new Label("Audio Tools screen coming soon"));
    }
}