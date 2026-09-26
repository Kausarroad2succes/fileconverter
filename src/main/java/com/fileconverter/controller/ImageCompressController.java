package com.fileconverter.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.fileconverter.service.ImageService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageCompressController {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png");

    @FXML
    private ListView<String> fileListView;

    @FXML
    private Label outputFolderLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label qualityLabel;

    @FXML
    private VBox dropZone;

    @FXML
    private Button compressButton;

    @FXML
    private ProgressBar compressProgressBar;

    @FXML
    private Slider qualitySlider;

    private final List<File> selectedFiles = new ArrayList<>();
    private File outputFolder;

    @FXML
    public void initialize() {
        setupDropZoneDragAndDrop();
        qualitySlider.valueProperty().addListener((obs, oldVal, newVal) ->
                qualityLabel.setText("Quality: " + newVal.intValue() + "%"));
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return ALLOWED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private void setupDropZoneDragAndDrop() {
        dropZone.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    if (isImageFile(file)) {
                        selectedFiles.add(file);
                        fileListView.getItems().add(file.getName());
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    private void handleAddFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image Files");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image files", "*.jpg", "*.jpeg", "*.png"));
        Stage stage = (Stage) fileListView.getScene().getWindow();
        List<File> chosenFiles = fileChooser.showOpenMultipleDialog(stage);
        if (chosenFiles != null) {
            for (File file : chosenFiles) {
                selectedFiles.add(file);
                fileListView.getItems().add(file.getName());
            }
        }
    }

    @FXML
    private void handleRemoveSelected() {
        int idx = fileListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            fileListView.getItems().remove(idx);
            selectedFiles.remove(idx);
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
    private void handleCompress() {
        if (selectedFiles.isEmpty()) {
            statusLabel.setText("Add at least one image");
            return;
        }
        if (outputFolder == null) {
            statusLabel.setText("Choose an output folder");
            return;
        }

        float quality = (float) (qualitySlider.getValue() / 100.0);
        List<File> filesToCompress = new ArrayList<>(selectedFiles);
        File targetFolder = outputFolder;

        Task<List<File>> compressTask = new Task<>() {
            @Override
            protected List<File> call() throws Exception {
                return ImageService.compressImages(filesToCompress, quality, targetFolder);
            }
        };

        compressButton.setDisable(true);
        compressProgressBar.setVisible(true);
        compressProgressBar.setManaged(true);
        compressProgressBar.progressProperty().bind(compressTask.progressProperty());
        statusLabel.setText("Compressing...");

        compressTask.setOnSucceeded(e -> {
            compressProgressBar.progressProperty().unbind();
            compressProgressBar.setVisible(false);
            compressProgressBar.setManaged(false);
            compressButton.setDisable(false);
            statusLabel.setText("Compressed " + compressTask.getValue().size() + " file(s) successfully!");
        });

        compressTask.setOnFailed(e -> {
            compressProgressBar.progressProperty().unbind();
            compressProgressBar.setVisible(false);
            compressProgressBar.setManaged(false);
            compressButton.setDisable(false);
            Throwable ex = compressTask.getException();
            statusLabel.setText("Compression failed: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        Thread thread = new Thread(compressTask, "image-compress-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleBackFromCompress() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fileconverter/fxml/main.fxml"));
            Parent root = loader.load();

            Node source = (Node) fileListView;
            Stage stage = (Stage) source.getScene().getWindow();

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            stage.setTitle("File converter");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}