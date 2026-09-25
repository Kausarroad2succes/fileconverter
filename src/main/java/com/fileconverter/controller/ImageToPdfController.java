package com.fileconverter.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.fileconverter.service.PdfService;

import java.io.IOException;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class ImageToPdfController {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png");

    @FXML
    private ListView<String> fileListView;

    @FXML
    private Label outputPathLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox dropZone;

    @FXML
    private Button convertButton;

    @FXML
    private ProgressBar convertProgressBar;

    private final List<File> selectedFiles = new ArrayList<>();
    private File outputFile;

    @FXML
    public void initialize() {
        setupDropZoneDragAndDrop();
        setupDragToReorder();
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

    private void setupDragToReorder() {
        fileListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };

            cell.setOnDragDetected(event -> {
                if (cell.getItem() == null) return;
                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(cell.getIndex()));
                db.setContent(content);
                event.consume();
            });

            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    int targetIndex = cell.getIndex();

                    Collections.swap(fileListView.getItems(), draggedIndex, targetIndex);
                    Collections.swap(selectedFiles, draggedIndex, targetIndex);

                    fileListView.getSelectionModel().select(targetIndex);
                    event.setDropCompleted(true);
                } else {
                    event.setDropCompleted(false);
                }
                event.consume();
            });

            return cell;
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
    private void handleChooseOutput() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Output PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF file", "*.pdf"));
        Stage stage = (Stage) fileListView.getScene().getWindow();
        File chosenFile = fileChooser.showSaveDialog(stage);
        if (chosenFile != null) {
            outputFile = chosenFile;
            outputPathLabel.setText(chosenFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleConvert() {
        if (selectedFiles.isEmpty()) {
            statusLabel.setText("Add at least one image");
            return;
        }
        if (outputFile == null) {
            statusLabel.setText("Select the output file");
            return;
        }

        List<File> filesToConvert = new ArrayList<>(selectedFiles);
        File targetFile = outputFile;

        Task<Void> convertTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                PdfService.imagesToPdf(filesToConvert, targetFile);
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
        });

        convertTask.setOnFailed(e -> {
            convertProgressBar.progressProperty().unbind();
            convertProgressBar.setVisible(false);
            convertProgressBar.setManaged(false);
            convertButton.setDisable(false);
            Throwable ex = convertTask.getException();
            statusLabel.setText("Conversion failed: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        Thread thread = new Thread(convertTask, "image-to-pdf-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleMoveUp() {
        int idx = fileListView.getSelectionModel().getSelectedIndex();
        if (idx > 0) {
            Collections.swap(fileListView.getItems(), idx, idx - 1);
            Collections.swap(selectedFiles, idx, idx - 1);
            fileListView.getSelectionModel().select(idx - 1);
        }
    }

    @FXML
    private void handleMoveDown() {
        int idx = fileListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < fileListView.getItems().size() - 1) {
            Collections.swap(fileListView.getItems(), idx, idx + 1);
            Collections.swap(selectedFiles, idx, idx + 1);
            fileListView.getSelectionModel().select(idx + 1);
        }
    }

    @FXML
    private void handleBackFromImageToPdf() {
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