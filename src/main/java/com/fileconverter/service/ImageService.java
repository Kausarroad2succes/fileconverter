package com.fileconverter.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageService {

    /**
     * Converts a batch of images to the target format ("png" or "jpg"),
     * writing results into outputFolder with the same base filename.
     *
     * @param imageFiles   source image files (jpg/jpeg/png)
     * @param targetFormat "png" or "jpg"
     * @param outputFolder folder to write converted files into
     * @return the files that were written, same order as imageFiles
     */
    public static List<File> convertImages(List<File> imageFiles, String targetFormat, File outputFolder) throws IOException {
        List<File> outputFiles = new ArrayList<>();
        String format = targetFormat.toLowerCase();

        for (File imageFile : imageFiles) {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                throw new IOException("Could not read image: " + imageFile.getName());
            }

            // JPG has no alpha channel — flatten to RGB (white background) if needed
            if (format.equals("jpg") && image.getColorModel().hasAlpha()) {
                BufferedImage flattened = new BufferedImage(
                        image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                flattened.createGraphics().drawImage(image, 0, 0, java.awt.Color.WHITE, null);
                image = flattened;
            }

            String baseName = stripExtension(imageFile.getName());
            File outFile = new File(outputFolder, baseName + "." + format);

            String writerFormat = format.equals("jpg") ? "jpg" : "png";
            if (!ImageIO.write(image, writerFormat, outFile)) {
                throw new IOException("No writer available for format: " + format);
            }

            outputFiles.add(outFile);
        }

        return outputFiles;
    }

    private static String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }


    /**
     * Resizes a batch of images to the given target width, preserving each image's
     * own aspect ratio if maintainAspectRatio is true (targetHeight is ignored in that case).
     * If maintainAspectRatio is false, both targetWidth and targetHeight are used exactly.
     * Output files keep their original format and filename, written into outputFolder.
     */
    public static List<File> resizeImages(List<File> imageFiles, int targetWidth, int targetHeight,
                                          boolean maintainAspectRatio, File outputFolder) throws IOException {
        List<File> outputFiles = new ArrayList<>();

        for (File imageFile : imageFiles) {
            BufferedImage original = ImageIO.read(imageFile);
            if (original == null) {
                throw new IOException("Could not read image: " + imageFile.getName());
            }

            int newWidth = targetWidth;
            int newHeight = targetHeight;

            if (maintainAspectRatio) {
                double aspectRatio = (double) original.getHeight() / original.getWidth();
                newHeight = (int) Math.round(targetWidth * aspectRatio);
            }

            BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType() == 0
                    ? BufferedImage.TYPE_INT_ARGB : original.getType());
            java.awt.Graphics2D g = resized.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, newWidth, newHeight, null);
            g.dispose();

            String format = getExtension(imageFile.getName());
            File outFile = new File(outputFolder, imageFile.getName());

            String writerFormat = format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg") ? "jpg" : "png";
            if (!ImageIO.write(resized, writerFormat, outFile)) {
                throw new IOException("No writer available for format: " + format);
            }

            outputFiles.add(outFile);
        }

        return outputFiles;
    }

    private static String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(dot + 1) : "png";
    }
}