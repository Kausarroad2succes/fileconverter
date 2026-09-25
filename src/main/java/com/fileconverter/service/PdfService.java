package com.fileconverter.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;

public class PdfService {

    private static String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    public static void mergePdfs(List<File> chosenFiles, File output) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        for (File chosen : chosenFiles) {
            merger.addSource(chosen);
        }
        merger.setDestinationFileName(output.getAbsolutePath());
        merger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache());
    }

    public static List<File> splitPdf(File sourceFile, List<int[]> pageRanges, File outputFolder) throws IOException {
        List<File> outputFiles = new ArrayList<>();
        String baseName = stripExtension(sourceFile.getName());

        try (PDDocument sourceDoc = Loader.loadPDF(sourceFile)) {
            int totalPages = sourceDoc.getNumberOfPages();

            for (int[] range : pageRanges) {
                int start = range[0];
                int end = range[1];

                if (start < 1 || end > totalPages || start > end) {
                    throw new IllegalArgumentException(
                            "Invalid page range " + start + "-" + end + " for a document with " + totalPages + " pages");
                }

                try (PDDocument splitDoc = new PDDocument()) {
                    for (int i = start; i <= end; i++) {
                        PDPage page = sourceDoc.getPage(i - 1);
                        splitDoc.importPage(page);
                    }

                    File outFile = new File(outputFolder, baseName + "_" + start + "-" + end + ".pdf");
                    splitDoc.save(outFile);
                    outputFiles.add(outFile);
                }
            }
        }

        return outputFiles;
    }

    public static void imagesToPdf(List<File> imageFiles, File outputFile) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            for (File imageFile : imageFiles) {
                PDImageXObject image = PDImageXObject.createFromFile(imageFile.getAbsolutePath(), doc);

                float imgWidth = image.getWidth();
                float imgHeight = image.getHeight();

                PDRectangle pageSize = imgWidth > imgHeight
                        ? new PDRectangle(Math.max(imgWidth, PDRectangle.A4.getHeight()), Math.max(imgHeight, PDRectangle.A4.getWidth()))
                        : PDRectangle.A4;

                float pageWidth = pageSize.getWidth();
                float pageHeight = pageSize.getHeight();
                float scale = Math.min(pageWidth / imgWidth, pageHeight / imgHeight);
                float drawWidth = imgWidth * scale;
                float drawHeight = imgHeight * scale;
                float x = (pageWidth - drawWidth) / 2f;
                float y = (pageHeight - drawHeight) / 2f;

                PDPage page = new PDPage(pageSize);
                doc.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                    contentStream.drawImage(image, x, y, drawWidth, drawHeight);
                }
            }

            doc.save(outputFile);
        }
    }


}