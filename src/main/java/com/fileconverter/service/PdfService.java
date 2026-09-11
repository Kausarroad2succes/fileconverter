package com.fileconverter.service;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.util.List;
import java.io.File;
import java.io.IOException;

public class PdfService {

    public static void mergePdfs(List<File> chosenFiles, File output) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        for (File chosen : chosenFiles) {
            merger.addSource(chosen);
        }
        merger.setDestinationFileName(output.getAbsolutePath());
        merger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache());
    }
}