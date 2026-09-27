package com.fileconverter.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PptxService {

    /**
     * Renders each PPTX slide as an image and embeds it as one PDF page.
     * Text/shapes are rasterized, not preserved as editable content.
     */
    public static void pptxToPdf(File pptxFile, File outputPdf) throws IOException {
        try (FileInputStream fis = new FileInputStream(pptxFile);
             XMLSlideShow ppt = new XMLSlideShow(fis);
             PDDocument pdfDoc = new PDDocument()) {

            Dimension pageSize = ppt.getPageSize();
            int scale = 2; // render at 2x for sharper output

            for (XSLFSlide slide : ppt.getSlides()) {
                BufferedImage img = new BufferedImage(
                        pageSize.width * scale, pageSize.height * scale, BufferedImage.TYPE_INT_RGB);

                Graphics2D graphics = img.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
                graphics.scale(scale, scale);
                slide.draw(graphics);
                graphics.dispose();

                PDRectangle pdfPageSize = new PDRectangle(pageSize.width, pageSize.height);
                PDPage page = new PDPage(pdfPageSize);
                pdfDoc.addPage(page);

                PDImageXObject pdImage = LosslessFactory.createFromImage(pdfDoc, img);
                try (PDPageContentStream contentStream = new PDPageContentStream(pdfDoc, page)) {
                    contentStream.drawImage(pdImage, 0, 0, pageSize.width, pageSize.height);
                }
            }

            pdfDoc.save(outputPdf);
        }
    }

    /**
     * Renders each PDF page as an image and places it as a full-slide picture in a new PPTX.
     * Text is not editable in the output, only visually reproduced.
     */
    public static void pdfToPptx(File pdfFile, File outputPptx, int dpi) throws IOException {
        try (PDDocument pdfDoc = Loader.loadPDF(pdfFile);
             XMLSlideShow ppt = new XMLSlideShow()) {

            ppt.setPageSize(new Dimension(960, 540)); // 13.33in x 7.5in widescreen, in points

            PDFRenderer renderer = new PDFRenderer(pdfDoc);
            int totalPages = pdfDoc.getNumberOfPages();

            for (int i = 0; i < totalPages; i++) {
                BufferedImage pageImage = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);

                XSLFSlide slide = ppt.createSlide();
                byte[] imageBytes = bufferedImageToPngBytes(pageImage);
                XSLFPictureData pictureData = ppt.addPicture(imageBytes, PictureData.PictureType.PNG);
                XSLFPictureShape picture = slide.createPicture(pictureData);

                Dimension slideSize = ppt.getPageSize();
                double imgRatio = (double) pageImage.getWidth() / pageImage.getHeight();
                double slideRatio = (double) slideSize.width / slideSize.height;

                double drawWidth;
                double drawHeight;
                if (imgRatio > slideRatio) {
                    drawWidth = slideSize.width;
                    drawHeight = drawWidth / imgRatio;
                } else {
                    drawHeight = slideSize.height;
                    drawWidth = drawHeight * imgRatio;
                }
                double x = (slideSize.width - drawWidth) / 2.0;
                double y = (slideSize.height - drawHeight) / 2.0;

                picture.setAnchor(new Rectangle2D.Double(x, y, drawWidth, drawHeight));
            }

            try (FileOutputStream out = new FileOutputStream(outputPptx)) {
                ppt.write(out);
            }
        }
    }

    private static byte[] bufferedImageToPngBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}