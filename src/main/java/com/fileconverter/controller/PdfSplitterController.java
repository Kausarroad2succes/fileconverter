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
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.fileconverter.service.PdfService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfSplitterController {

    @FXML
    private Label sourceFileLabel;

    @FXML
    private Label outputFolderLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField rangesField;

    @FXML
    private Button splitButton;

    @FXML
    private ProgressBar splitProgressBar;

    private File sourceFile;
    private File outputFolder;

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
    private void handleSplit() {
        if (sourceFile == null) {
            statusLabel.setText("Choose a source PDF");
            return;
        }
        if (outputFolder == null) {
            statusLabel.setText("Choose an output folder");
            return;
        }

        List<int[]> ranges;
        try {
            ranges = parseRanges(rangesField.getText());
        } catch (IllegalArgumentException e) {
            statusLabel.setText("Invalid ranges: " + e.getMessage());
            return;
        }

        if (ranges.isEmpty()) {
            statusLabel.setText("Enter at least one page range");
            return;
        }

        File finalSourceFile = sourceFile;
        File finalOutputFolder = outputFolder;

        Task<List<File>> splitTask = new Task<>() {
            @Override
            protected List<File> call() throws Exception {
                return PdfService.splitPdf(finalSourceFile, ranges, finalOutputFolder);
            }
        };

        splitButton.setDisable(true);
        splitProgressBar.setVisible(true);
        splitProgressBar.setManaged(true);
        splitProgressBar.progressProperty().bind(splitTask.progressProperty());
        statusLabel.setText("Splitting...");

        splitTask.setOnSucceeded(e -> {
            splitProgressBar.progressProperty().unbind();
            splitProgressBar.setVisible(false);
            splitProgressBar.setManaged(false);
            splitButton.setDisable(false);
            statusLabel.setText("Split successful! " + splitTask.getValue().size() + " file(s) created.");
        });

        splitTask.setOnFailed(e -> {
            splitProgressBar.progressProperty().unbind();
            splitProgressBar.setVisible(false);
            splitProgressBar.setManaged(false);
            splitButton.setDisable(false);
            Throwable ex = splitTask.getException();
            statusLabel.setText("Split failed: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        Thread thread = new Thread(splitTask, "pdf-split-thread");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Parses "1-3, 4-6, 7" into [[1,3],[4,6],[7,7]].
     * Throws IllegalArgumentException with a user-facing message on bad input.
     */
    private List<int[]> parseRanges(String text) {
        List<int[]> ranges = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return ranges;
        }

        String[] parts = text.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;

            int start;
            int end;
            if (trimmed.contains("-")) {
                String[] bounds = trimmed.split("-");
                if (bounds.length != 2) {
                    throw new IllegalArgumentException("bad range \"" + trimmed + "\"");
                }
                try {
                    start = Integer.parseInt(bounds[0].trim());
                    end = Integer.parseInt(bounds[1].trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("bad range \"" + trimmed + "\"");
                }
            } else {
                try {
                    start = end = Integer.parseInt(trimmed);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("bad page number \"" + trimmed + "\"");
                }
            }

            if (start > end) {
                throw new IllegalArgumentException("\"" + trimmed + "\" starts after it ends");
            }

            ranges.add(new int[]{start, end});
        }

        return ranges;
    }

    @FXML
    private void handleBackFromSplit() {
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