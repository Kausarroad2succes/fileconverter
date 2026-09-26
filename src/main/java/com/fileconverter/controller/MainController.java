package com.fileconverter.controller;

import com.fileconverter.model.RecentFileEntry;
import com.fileconverter.service.RecentFilesService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML
    private ListView<RecentFileEntry> recentFilesListView;

    @FXML
    private Button clearRecentButton;

    @FXML
    public void initialize() {
        loadRecentFiles();

        recentFilesListView.setCellFactory(lv -> new RecentFileCell());

        recentFilesListView.setOnMouseClicked(event -> {
            RecentFileEntry selected = recentFilesListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openContainingFolder(selected.getFilePath());
            }
        });
    }

    private void loadRecentFiles() {
        List<RecentFileEntry> recent = RecentFilesService.getRecentFiles(10);
        recentFilesListView.getItems().setAll(recent);
    }

    private void openContainingFolder(String filePath) {
        try {
            File file = new File(filePath);
            File folder = file.getParentFile();
            if (folder != null && folder.exists()) {
                Desktop.getDesktop().open(folder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearRecentFiles() {
        RecentFilesService.clearAllRecentFiles();
        loadRecentFiles();
    }

    /** Row: filename (tool name) on the left, a small Delete button on the right. */
    private class RecentFileCell extends ListCell<RecentFileEntry> {
        private final javafx.scene.control.Label label = new javafx.scene.control.Label();
        private final Button deleteButton = new Button("✕");
        private final HBox container = new HBox(8, label, spacer(), deleteButton);

        RecentFileCell() {
            deleteButton.setOnAction(e -> {
                RecentFileEntry item = getItem();
                if (item != null) {
                    RecentFilesService.deleteRecentFile(item.getId());
                    loadRecentFiles();
                }
            });
        }

        private HBox spacer() {
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return spacer;
        }

        @Override
        protected void updateItem(RecentFileEntry item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                label.setText(item.getFileName() + "  (" + item.getToolName() + ")");
                setGraphic(container);
            }
        }
    }

    // ---- Card grid handlers (MouseEvent) ----
    @FXML
    private void openPDFMerger(MouseEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/pdf-merger.fxml", "PDF Merger");
    }

    @FXML
    private void openSplitPdf(MouseEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/pdf-splitter.fxml", "Split PDF");
    }

    @FXML
    private void openImageToPdf(MouseEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/image-to-pdf.fxml", "Image to PDF");
    }

    @FXML
    private void openPdfToImage(MouseEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/pdf-to-image.fxml", "PDF to Image");
    }

    @FXML
    private void openJpgToPng(MouseEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/jpg-png-converter.fxml", "JPG ↔ PNG Converter");
    }

    @FXML
    private void openResizeImage(MouseEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/image-resize.fxml", "Image Resize");
    }

    @FXML
    private void openCompressImage(MouseEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/image-compress.fxml", "Compress Image");
    }

    @FXML
    private void openRearrangePdfPage(MouseEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/pdf-rearrange.fxml", "Rearrange PDF Pages");
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

    @FXML
    private void openImageToPdfFromButton(ActionEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/image-to-pdf.fxml", "Image to PDF");
    }

    @FXML
    private void openPdfToImageFromButton(ActionEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/pdf-to-image.fxml", "PDF to Image");
    }

    @FXML
    private void openJpgToPngFromButton(ActionEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/jpg-png-converter.fxml", "JPG ↔ PNG Converter");
    }

    @FXML
    private void openResizeImageFromButton(ActionEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/image-resize.fxml", "Image Resize");
    }

    @FXML
    private void openCompressImageFromButton(ActionEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/image-compress.fxml", "Compress Image");
    }

    @FXML
    private void openRearrangePdfPageFromButton(ActionEvent event) {
        switchScene(event.getSource(), "/com/fileconverter/fxml/pdf-rearrange.fxml", "Rearrange PDF Pages");
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