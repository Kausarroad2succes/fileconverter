package com.fileconverter.controller;

import com.fileconverter.model.PageItem;
import com.fileconverter.service.PdfService;
import com.fileconverter.service.RecentFilesService;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PdfRearrangeController {

    @FXML
    private ListView<PageItem> pageListView;

    @FXML
    private Label sourceFileLabel;

    @FXML
    private Label outputPathLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button saveButton;

    @FXML
    private ProgressBar loadProgressBar;

    @FXML
    private ProgressBar saveProgressBar;

    private File sourceFile;
    private File outputFile;

    @FXML
    public void initialize() {
        setupDragToReorder();
    }

    private void setupDragToReorder() {
        pageListView.setCellFactory(lv -> {
            PageListCell cell = new PageListCell();

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

                    if (targetIndex >= 0 && targetIndex < pageListView.getItems().size()) {
                        Collections.swap(pageListView.getItems(), draggedIndex, targetIndex);
                        pageListView.getSelectionModel().select(targetIndex);
                        event.setDropCompleted(true);
                    } else {
                        event.setDropCompleted(false);
                    }
                } else {
                    event.setDropCompleted(false);
                }
                event.consume();
            });

            return cell;
        });
    }

    @FXML
    private void handleChooseSourceFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF file", "*.pdf"));
        Stage stage = (Stage) sourceFileLabel.getScene().getWindow();
        File chosenFile = fileChooser.showOpenDialog(stage);
        if (chosenFile == null) {
            return;
        }

        sourceFile = chosenFile;
        sourceFileLabel.setText(chosenFile.getAbsolutePath());
        pageListView.getItems().clear();
        statusLabel.setText("");

        Task<List<PageItem>> loadTask = new Task<>() {
            @Override
            protected List<PageItem> call() throws Exception {
                List<BufferedImage> thumbnails = PdfService.renderPageThumbnails(chosenFile, 60);
                List<PageItem> items = new ArrayList<>();
                for (int i = 0; i < thumbnails.size(); i++) {

                    Image fxImage = SwingFXUtils.toFXImage(thumbnails.get(i), null);
                    items.add(new PageItem(i, fxImage));
                }
                return items;
            }
        };

        loadProgressBar.setVisible(true);
        loadProgressBar.setManaged(true);
        loadProgressBar.progressProperty().bind(loadTask.progressProperty());
        statusLabel.setText("Loading pages...");

        loadTask.setOnSucceeded(e -> {
            loadProgressBar.progressProperty().unbind();
            loadProgressBar.setVisible(false);
            loadProgressBar.setManaged(false);
            pageListView.getItems().setAll(loadTask.getValue());
            statusLabel.setText(loadTask.getValue().size() + " page(s) loaded. Drag to reorder.");
        });

        loadTask.setOnFailed(e -> {
            loadProgressBar.progressProperty().unbind();
            loadProgressBar.setVisible(false);
            loadProgressBar.setManaged(false);
            Throwable ex = loadTask.getException();
            statusLabel.setText("Failed to load PDF: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        Thread thread = new Thread(loadTask, "pdf-thumbnail-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleChooseOutput() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Reordered PDF As");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF file", "*.pdf"));
        Stage stage = (Stage) outputPathLabel.getScene().getWindow();
        File chosenFile = fileChooser.showSaveDialog(stage);
        if (chosenFile != null) {
            outputFile = chosenFile;
            outputPathLabel.setText(chosenFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleSave() {
        if (sourceFile == null || pageListView.getItems().isEmpty()) {
            statusLabel.setText("Choose a source PDF first");
            return;
        }
        if (outputFile == null) {
            statusLabel.setText("Choose an output file");
            return;
        }

        List<Integer> newOrder = new ArrayList<>();
        for (PageItem item : pageListView.getItems()) {
            newOrder.add(item.getOriginalIndex());
        }

        File finalSourceFile = sourceFile;
        File finalOutputFile = outputFile;

        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                PdfService.reorderPages(finalSourceFile, newOrder, finalOutputFile);
                return null;
            }
        };

        saveButton.setDisable(true);
        saveProgressBar.setVisible(true);
        saveProgressBar.setManaged(true);
        saveProgressBar.progressProperty().bind(saveTask.progressProperty());
        statusLabel.setText("Saving...");

        saveTask.setOnSucceeded(e -> {
            saveProgressBar.progressProperty().unbind();
            saveProgressBar.setVisible(false);
            saveProgressBar.setManaged(false);
            saveButton.setDisable(false);
            statusLabel.setText("Saved successfully!");
            RecentFilesService.addRecentFile(finalOutputFile.getAbsolutePath(), "Rearrange PDF Pages");
        });

        saveTask.setOnFailed(e -> {
            saveProgressBar.progressProperty().unbind();
            saveProgressBar.setVisible(false);
            saveProgressBar.setManaged(false);
            saveButton.setDisable(false);
            Throwable ex = saveTask.getException();
            statusLabel.setText("Save failed: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        Thread thread = new Thread(saveTask, "pdf-rearrange-save-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleBackFromRearrange() {
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

    private static class PageListCell extends ListCell<PageItem> {
        private final ImageView imageView = new ImageView();
        private final Label label = new Label();
        private final HBox container = new HBox(12, imageView, label);

        PageListCell() {
            imageView.setFitHeight(120);
            imageView.setPreserveRatio(true);
            container.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 6;");
        }

        @Override
        protected void updateItem(PageItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                imageView.setImage(item.getThumbnail());
                label.setText("Page " + item.getDisplayPageNumber());
                setGraphic(container);
            }
        }
    }
}