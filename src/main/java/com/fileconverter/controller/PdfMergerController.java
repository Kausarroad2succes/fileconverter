package com.fileconverter.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class PdfMergerController {

    @FXML
    private ListView<String> fileListView;

    @FXML
    private Label outputPathLabel;

    @FXML
    private Label statusLabel;

    // Keep the actual File objects here, in the same order shown in fileListView
    private final List<File> selectedFiles = new java.util.ArrayList<>();
    private File outputFile;

}