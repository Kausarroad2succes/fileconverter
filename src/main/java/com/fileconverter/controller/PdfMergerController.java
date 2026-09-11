package com.fileconverter.controller;


import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.fileconverter.service.PdfService;
import java.io.IOException;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class PdfMergerController {

    @FXML
    private ListView<String> fileListView;

    @FXML
    private Label outputPathLabel;

    @FXML
    private Label statusLabel;



    private final List<File> selectedFiles = new ArrayList<>();
    private File outputFile;

    /**
     *
     */
    @FXML
    private void handleAddFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF file", "*.pdf"));
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
    private void handleRemoveSelected(){
        int idx=fileListView.getSelectionModel().getSelectedIndex();
        if(idx>=0){
            fileListView.getItems().remove(idx);
            selectedFiles.remove(idx);
        }
    }

    @FXML
    private void handleChooseOutput() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF file", "*.pdf"));
        Stage stage = (Stage) fileListView.getScene().getWindow();
        File chosenFiles = fileChooser.showSaveDialog(stage);
        if (chosenFiles != null) {

               outputFile=chosenFiles;
                outputPathLabel.setText(chosenFiles.getAbsolutePath());

        }

    }

    @FXML
    private void handleMerge(){
        if(selectedFiles.isEmpty()){
            statusLabel.setText("Add at least one file");
        }
        else if(outputFile==null){
            statusLabel.setText("Select the ouput file");
        }
        else {
            try {
                PdfService.mergePdfs(selectedFiles, outputFile);
                statusLabel.setText("Merge successful!");
            } catch (IOException e) {
                statusLabel.setText("Merge failed: " + e.getMessage());
            }
        }
    }

}