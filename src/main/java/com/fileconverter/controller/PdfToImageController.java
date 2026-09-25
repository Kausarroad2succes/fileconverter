package com.fileconverter.controller;

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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.fileconverter.service.PdfService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PdfToImageController {

    @FXML
    private Label sourceFileLabel;

    @FXML
    private Label outputFolderLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button convertButton;

    @FXML
    private ProgressBar convertProgressBar;

    @FXML
    private RadioButton pngRadio;

    @FXML
    private RadioButton jpgRadio;

    @FXML
    private Spinner<Integer> dpiSpinner;

    private File sourceFile;
    private File outputFolder;

    @FXML
    public void initialize() {
        dpiSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(72, 600, 150, 10));
    }

    @FXML
    private void handleChooseSourceFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF file", "*.pdf"));
        Stage stage = (Stage) sourceFileLabel.getScene().getWindow();
        File chosenFile = fileChooser.showOpenDialog(stage);
        if (chosenFile != null) {
            sourceFile = chosenFile;
            sourceFileLabel.setText(chosenFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleChooseOutputFolder() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Output Folder");
        Stage stage = (Stage) outputFolderLabel.getScene().getWindow();
        File chosenDir = dirChooser.showDialog(stage);
        if (chosenDir != null) {
            outputFolder = chosenDir;
            outputFolderLabel.setText(chosenDir.getAbsolutePath());
        }
    }

    @FXML
    private void handleConvert() {
        if (sourceFile == null) {
            statusLabel.setText("Choose a source PDF");
            return;
        }
        if (outputFolder == null) {
            statusLabel.setText("Choose an output folder");
            return;
        }

        String format = jpgRadio.isSelected() ? "jpg" : "png";
        int dpi = dpiSpinner.getValue();
        File finalSourceFile = sourceFile;
        File finalOutputFolder = outputFolder;

        Task<List<File>> convertTask = new Task<>() {
            @Override
            protected List<File> call() throws Exception {
                return PdfService.pdfToImages(finalSourceFile, format, dpi, finalOutputFolder);
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
            statusLabel.setText("Exported " + convertTask.getValue().size() + " page(s) successfully!");
        });

        convertTask.setOnFailed(e -> {
            convertProgressBar.progressProperty().unbind();
            convertProgressBar.setVisible(false);
            convertProgressBar.setManaged(false);
            convertButton.setDisable(false);
            Throwable ex = convertTask.getException();
            statusLabel.setText("Conversion failed: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        Thread thread = new Thread(convertTask, "pdf-to-image-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleBackFromPdfToImage() {
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