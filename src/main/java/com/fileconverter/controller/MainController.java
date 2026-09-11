package com.fileconverter.controller;

import javafx.fxml.FXML;

public class MainController {

    @FXML
    private void openPDFMerger() {
        System.out.println("PDF Merger clicked");
    }

    @FXML
    private void openSplitPdf() {
        System.out.println("Split PDF clicked");
    }

    @FXML
    private void openImageToPdf() {
        System.out.println("Image to PDF clicked");
    }

    @FXML
    private void openJpgToPng() {
        System.out.println("JPG to PNG clicked");
    }

    @FXML
    private void openResizeImage() {
        System.out.println("Resize Image clicked");
    }

    @FXML
    private void openPdfToImage() {
        System.out.println("PDF to Image clicked");
    }

    @FXML
    private void openCompressImage() {
        System.out.println("Compress Image clicked");
    }

    @FXML
    private void openRotateFlip() {
        System.out.println("Rotate/Flip clicked");
    }
}