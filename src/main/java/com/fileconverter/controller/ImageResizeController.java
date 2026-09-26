package com.fileconverter.controller;

import com.fileconverter.service.RecentFilesService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
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

public class ImageResizeController {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png");

    @FXML
    private ListView<String> fileListView;

    @FXML
    private Label outputFolderLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox dropZone;

    @FXML
    private Button resizeButton;

    @FXML
    private ProgressBar resizeProgressBar;

    @FXML
    private TextField widthField;

    @FXML
    private TextField heightField;

    @FXML
    private CheckBox maintainAspectCheckBox;

    private final List<File> selectedFiles = new ArrayList<>();
    private File outputFolder;

    @FXML
    public void initialize() {
        setupDropZoneDragAndDrop();
        heightField.disableProperty().bind(maintainAspectCheckBox.selectedProperty());
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
    private void handleResize() {
        if (selectedFiles.isEmpty()) {
            statusLabel.setText("Add at least one image");
            return;
        }
        if (outputFolder == null) {
            statusLabel.setText("Choose an output folder");
            return;
        }

        int width;
        int height = 0;
        boolean maintainAspect = maintainAspectCheckBox.isSelected();

        try {
            width = Integer.parseInt(widthField.getText().trim());
            if (width <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            statusLabel.setText("Enter a valid width");
            return;
        }

        if (!maintainAspect) {
            try {
                height = Integer.parseInt(heightField.getText().trim());
                if (height <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                statusLabel.setText("Enter a valid height");
                return;
            }
        }

        List<File> filesToResize = new ArrayList<>(selectedFiles);
        File targetFolder = outputFolder;
        int finalWidth = width;
        int finalHeight = height;

        Task<List<File>> resizeTask = new Task<>() {
            @Override
            protected List<File> call() throws Exception {
                return ImageService.resizeImages(filesToResize, finalWidth, finalHeight, maintainAspect, targetFolder);
            }
        };

        resizeButton.setDisable(true);
        resizeProgressBar.setVisible(true);
        resizeProgressBar.setManaged(true);
        resizeProgressBar.progressProperty().bind(resizeTask.progressProperty());
        statusLabel.setText("Resizing...");

        resizeTask.setOnSucceeded(e -> {
            resizeProgressBar.progressProperty().unbind();
            resizeProgressBar.setVisible(false);
            resizeProgressBar.setManaged(false);
            resizeButton.setDisable(false);
            statusLabel.setText("Resized " + resizeTask.getValue().size() + " file(s) successfully!");

            for (File outputFile : resizeTask.getValue()) {
                RecentFilesService.addRecentFile(outputFile.getAbsolutePath(), "Image Resize");
            }
        });

        resizeTask.setOnFailed(e -> {
            resizeProgressBar.progressProperty().unbind();
            resizeProgressBar.setVisible(false);
            resizeProgressBar.setManaged(false);
            resizeButton.setDisable(false);
            Throwable ex = resizeTask.getException();
            statusLabel.setText("Resize failed: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        Thread thread = new Thread(resizeTask, "image-resize-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleBackFromResize() {
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