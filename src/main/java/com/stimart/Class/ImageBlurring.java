package com.stimart.Class;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.util.ArrayList;

public class ImageBlurring {

    public static ArrayList<Image> splitImage(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        int halfWidth = width / 2;
        int halfHeight = height / 2;

        PixelReader reader = image.getPixelReader();

        WritableImage topLeft = new WritableImage(reader, 0, 0, halfWidth, halfHeight);
        WritableImage topRight = new WritableImage(reader, halfWidth, 0, halfWidth, halfHeight);
        WritableImage bottomLeft = new WritableImage(reader, 0, halfHeight, halfWidth, halfHeight);
        WritableImage bottomRight = new WritableImage(reader, halfWidth, halfHeight, halfWidth, halfHeight);

        ArrayList<Image> imageList = new ArrayList<>();
        imageList.add(topLeft);
        imageList.add(topRight);
        imageList.add(bottomLeft);
        imageList.add(bottomRight);

        return imageList;
    }

    public static Color calculateAverageColor(Image image) {
        PixelReader pixelReader = image.getPixelReader();
        if (pixelReader == null) {
            throw new IllegalArgumentException("Image does not have a PixelReader.");
        }

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        long totalRed = 0;
        long totalGreen = 0;
        long totalBlue = 0;
        int pixelCount = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                totalRed += (long) (color.getRed() * 255);
                totalGreen += (long) (color.getGreen() * 255);
                totalBlue += (long) (color.getBlue() * 255);
                pixelCount++;
            }
        }

        double avgRed = (double) totalRed / pixelCount / 255;
        double avgGreen = (double) totalGreen / pixelCount / 255;
        double avgBlue = (double) totalBlue / pixelCount / 255;

        return new Color(avgRed, avgGreen, avgBlue, 1.0);
    }

    public static Image applyAverageColor(Image image, Color averageColor) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setColor(x, y, averageColor);
            }
        }

        return writableImage;
    }

    public static Image mergeImage(ArrayList<Image> images) {
        if (images == null || images.size() != 4) {
            throw new IllegalArgumentException("ArrayList must contain exactly 4 images.");
        }

        int halfWidth = (int) images.get(0).getWidth();
        int halfHeight = (int) images.get(0).getHeight();

        WritableImage mergedImage = new WritableImage(halfWidth * 2, halfHeight * 2);
        PixelWriter writer = mergedImage.getPixelWriter();

        for (int y = 0; y < halfHeight; y++) {
            for (int x = 0; x < halfWidth; x++) {
                // Top-left
                writer.setArgb(x, y, images.get(0).getPixelReader().getArgb(x, y));
                // Top-right
                writer.setArgb(x + halfWidth, y, images.get(1).getPixelReader().getArgb(x, y));
                // Bottom-left
                writer.setArgb(x, y + halfHeight, images.get(2).getPixelReader().getArgb(x, y));
                // Bottom-right
                writer.setArgb(x + halfWidth, y + halfHeight, images.get(3).getPixelReader().getArgb(x, y));
            }
        }

        return mergedImage;
    }

    public static Image blurImageCaller(ExternalImages image, int level) {
        if (level == 0) {
            return image.originalImage;
        }
        else {
            int determine = (int) (Math.min(image.image.getHeight(), image.image.getWidth()));
            int i;
            for (i = 1; i < level; i++) {
                determine /= 2;
                if (determine <= 1) break;
            }
            return blurImage(image.originalImage, i);
        }
    }

    public static Image blurImage(Image image, int level) {
        if (level == 1) {
            return applyAverageColor(image, calculateAverageColor(image));
        }
        else {
            ArrayList<Image> temp = splitImage(image);
            for (int i = 0; i < temp.size(); i++) {
                temp.set(i, blurImage(temp.get(i), level - 1));
            }
            return mergeImage(temp);
        }
    }
}
