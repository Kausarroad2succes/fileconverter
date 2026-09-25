package com.fileconverter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    // ---- Card grid handlers (MouseEvent) ----
    @FXML
    private void openPDFMerger(MouseEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/pdf-merger.fxml", "PDF Merger");
    }

    @FXML
    private void openSplitPdf(MouseEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/pdf-splitter.fxml", "Split PDF");
    }



    // ---- Sidebar button handlers (ActionEvent) — just call the same logic ----
    @FXML
    private void openPDFMergerFromButton(ActionEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/pdf-merger.fxml", "PDF Merger");
    }

    @FXML
    private void openSplitPdfFromButton(ActionEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/pdf-splitter.fxml", "Split PDF");
    }



    // ---- Shared scene-switch logic, now takes a plain source object ----
    private void switchScene(Object source, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) source).getScene().getWindow();

            Scene currentScene = stage.getScene();

            currentScene.setRoot(root);
            stage.setTitle(title);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}