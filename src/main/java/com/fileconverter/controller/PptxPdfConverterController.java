package com.fileconverter.controller;

import com.fileconverter.service.PptxService;
import com.fileconverter.service.RecentFilesService;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class PptxPdfConverterController {

    @FXML
    private Label sourceFileLabel;

    @FXML
    private Label outputPathLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button convertButton;

    @FXML
    private ProgressBar convertProgressBar;

    @FXML
    private RadioButton pptxToPdfRadio;

    @FXML
    private RadioButton pdfToPptxRadio;

    @FXML
    private Spinner<Integer> dpiSpinner;

    private File sourceFile;
    private File outputFile;

    @FXML
    public void initialize() {
        dpiSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(72, 600, 150, 10));
    }

    @FXML
    private void handleChooseSourceFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PowerPoint file", "*.pptx"),
                new FileChooser.ExtensionFilter("PDF file", "*.pdf"));
        Stage stage = (Stage) sourceFileLabel.getScene().getWindow();
        File chosenFile = fileChooser.showOpenDialog(stage);
        if (chosenFile != null) {
            sourceFile = chosenFile;
            sourceFileLabel.setText(chosenFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleChooseOutput() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        if (pptxToPdfRadio.isSelected()) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF file", "*.pdf"));
        } else {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PowerPoint file", "*.pptx"));
        }
        Stage stage = (Stage) outputPathLabel.getScene().getWindow();
        File chosenFile = fileChooser.showSaveDialog(stage);
        if (chosenFile != null) {
            outputFile = chosenFile;
            outputPathLabel.setText(chosenFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleConvert() {
        if (sourceFile == null) {
            statusLabel.setText("Choose a source file");
            return;
        }
        if (outputFile == null) {
            statusLabel.setText("Choose an output file");
            return;
        }

        boolean pptxToPdf = pptxToPdfRadio.isSelected();
        int dpi = dpiSpinner.getValue();
        File finalSourceFile = sourceFile;
        File finalOutputFile = outputFile;

        Task<Void> convertTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (pptxToPdf) {
                    PptxService.pptxToPdf(finalSourceFile, finalOutputFile);
                } else {
                    PptxService.pdfToPptx(finalSourceFile, finalOutputFile, dpi);
                }
                return null;
            }
        };

        convertButton.setDisable(true);
        convertProgressBar.setVisible(true);
        convertProgressBar.setManaged(true);
        convertProgressBar.progressProperty().bind(convertTask.progressProperty());
        statusLabel.setText("Converting...");

        convertTask.setOnSucceeded(e -> {
            convertProgressBar.progressProperty().unbind();
            convertProgressBar.setVisible(false);
            convertProgressBar.setManaged(false);
            convertButton.setDisable(false);
            statusLabel.setText("Conversion successful!");
            RecentFilesService.addRecentFile(finalOutputFile.getAbsolutePath(),
                    pptxToPdf ? "PPTX to PDF" : "PDF to PPTX");
        });

        convertTask.setOnFailed(e -> {
            convertProgressBar.progressProperty().unbind();
            convertProgressBar.setVisible(false);
            convertProgressBar.setManaged(false);
            convertButton.setDisable(false);
            Throwable ex = convertTask.getException();
            statusLabel.setText("Conversion failed: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        Thread thread = new Thread(convertTask, "pptx-pdf-convert-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleBackFromConverter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fileconverter/fxml/main.fxml"));
            Parent root = loader.load();

            Node source = (Node) sourceFileLabel;
            Stage stage = (Stage) source.getScene().getWindow();

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            stage.setTitle("File converter");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}