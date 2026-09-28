package com.fileconverter.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    public static List<File> pdfToImages(File sourceFile, String format, int dpi, File outputFolder) throws IOException {
        List<File> outputFiles = new ArrayList<>();
        String baseName = stripExtension(sourceFile.getName());
        String writerFormat = format.equalsIgnoreCase("jpg") ? "jpg" : "png";

        try (PDDocument doc = Loader.loadPDF(sourceFile)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            int totalPages = doc.getNumberOfPages();

            for (int i = 0; i < totalPages; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);

                File outFile = new File(outputFolder, baseName + "_page" + (i + 1) + "." + writerFormat);
                if (!ImageIO.write(image, writerFormat, outFile)) {
                    throw new IOException("No writer available for format: " + writerFormat);
                }

                outputFiles.add(outFile);
            }
        }

        return outputFiles;
    }

    public static List<BufferedImage> renderPageThumbnails(File sourceFile, int dpi) throws IOException {
        List<BufferedImage> thumbnails = new ArrayList<>();
        try (PDDocument doc = Loader.loadPDF(sourceFile)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                thumbnails.add(renderer.renderImageWithDPI(i, dpi, ImageType.RGB));
            }
        }
        return thumbnails;
    }

    public static void reorderPages(File sourceFile, List<Integer> newPageOrder, File outputFile) throws IOException {
        try (PDDocument sourceDoc = Loader.loadPDF(sourceFile);
             PDDocument outputDoc = new PDDocument()) {

            int totalPages = sourceDoc.getNumberOfPages();
            for (int originalIndex : newPageOrder) {
                if (originalIndex < 0 || originalIndex >= totalPages) {
                    throw new IllegalArgumentException("Page index " + originalIndex + " is out of range");
                }
                outputDoc.importPage(sourceDoc.getPage(originalIndex));
            }

            outputDoc.save(outputFile);
        }
    }
    public static void docxToPdf(File sourceDocx, File outputPdf) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceDocx);
             XWPFDocument docxDoc = new XWPFDocument(fis);
             PDDocument pdfDoc = new PDDocument()) {

            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float fontSize = 11f;
            float leading = 14f;
            float margin = 50f;

            PDPage page = new PDPage(PDRectangle.LETTER);
            pdfDoc.addPage(page);
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float maxTextWidth = pageWidth - 2 * margin;

            PDPageContentStream contentStream = new PDPageContentStream(pdfDoc, page);
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(margin, pageHeight - margin);

            float yPosition = pageHeight - margin;

            for (XWPFParagraph paragraph : docxDoc.getParagraphs()) {
                String text = paragraph.getText();
                if (text == null) {
                    text = "";
                }

                List<String> wrappedLines = wrapText(text, font, fontSize, maxTextWidth);
                if (wrappedLines.isEmpty()) {
                    wrappedLines.add("");
                }

                for (String line : wrappedLines) {
                    if (yPosition - leading < margin) {
                        contentStream.endText();
                        contentStream.close();

                        page = new PDPage(PDRectangle.LETTER);
                        pdfDoc.addPage(page);
                        yPosition = pageHeight - margin;

                        contentStream = new PDPageContentStream(pdfDoc, page);
                        contentStream.beginText();
                        contentStream.setFont(font, fontSize);
                        contentStream.newLineAtOffset(margin, yPosition);
                    }

                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -leading);
                    yPosition -= leading;
                }
            }

            contentStream.endText();
            contentStream.close();

            pdfDoc.save(outputPdf);
        }
    }

    private static List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text.isEmpty()) {
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            float width = font.getStringWidth(candidate) / 1000 * fontSize;

            if (width > maxWidth && !currentLine.isEmpty()) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(candidate);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    public static void pdfToDocx(File sourceFile, File outputFile) throws IOException {
        try (PDDocument pdfDoc = Loader.loadPDF(sourceFile);
             XWPFDocument docxDoc = new XWPFDocument()) {

            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = pdfDoc.getNumberOfPages();

            for (int page = 1; page <= totalPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(pdfDoc);

                for (String line : pageText.split("\\r?\\n")) {
                    XWPFParagraph paragraph = docxDoc.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setText(line);
                }

                if (page < totalPages) {
                    XWPFParagraph pageBreak = docxDoc.createParagraph();
                    pageBreak.createRun().addBreak(BreakType.PAGE);
                }
            }

            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                docxDoc.write(out);
            }
        }
    }
}