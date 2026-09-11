package com.fileconverter.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private void openPDFMerger(MouseEvent event) {
        switchScene(event, "/com/fileconverter/fxml/pdf-merger.fxml", "PDF Merger");
    }

    @FXML
    private void openSplitPdf() {
        System.out.println("Split PDF clicked");
    }

    @FXML
    private void openImageToPdf() {
        System.out.println("Image to PDF clicked");
    }

    @FXML
    private void openJpgToPng() {
        System.out.println("JPG to PNG clicked");
    }

    @FXML
    private void openResizeImage() {
        System.out.println("Resize Image clicked");
    }

    @FXML
    private void openPdfToImage() {
        System.out.println("PDF to Image clicked");
    }

    @FXML
    private void openCompressImage() {
        System.out.println("Compress Image clicked");
    }

    @FXML
    private void openRotateFlip() {
        System.out.println("Rotate/Flip clicked");
    }

    private void switchScene(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            Scene scene = new Scene(root, 700, 500);
            stage.setScene(scene);
            stage.setTitle(title);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}