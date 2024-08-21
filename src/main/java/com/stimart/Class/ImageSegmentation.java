package com.stimart.Class;

import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class ImageSegmentation {
    public static void segmentImage(ArrayList<ExternalImages> externalImages, int selectedImage, Color color, double threshold) {
        int width = (int) externalImages.get(selectedImage).image.getWidth();
        int height = (int) externalImages.get(selectedImage).image.getHeight();
        WritableImage segmented = new WritableImage(width, height);
        WritableImage modifiedOriginal = new WritableImage(width, height);
        PixelWriter pwSegmented = segmented.getPixelWriter();
        PixelWriter pwOriginal = modifiedOriginal.getPixelWriter();
        PixelReader prOriginal = externalImages.get(selectedImage).image.getPixelReader();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (isColorCloseTo(prOriginal.getColor(j, i), color, threshold)) {
                    pwSegmented.setColor(j, i, prOriginal.getColor(j, i));
                }
                else {
                    pwOriginal.setColor(j, i, prOriginal.getColor(j, i));
                }
            }
        }

        ExternalImages newSegmented = new ExternalImages(segmented, externalImages.get(selectedImage).imageX, externalImages.get(selectedImage).imageY);
        ExternalImages newOriginal = new ExternalImages(modifiedOriginal, externalImages.get(selectedImage).imageX, externalImages.get(selectedImage).imageY);

        externalImages.remove(selectedImage);
        externalImages.add(selectedImage, newOriginal);
        externalImages.add(newSegmented);
    }

    public static boolean isColorCloseTo(Color colorToCheck, Color targetColor, double threshold) {
        double redDiff = Math.abs(colorToCheck.getRed() - targetColor.getRed());
        double greenDiff = Math.abs(colorToCheck.getGreen() - targetColor.getGreen());
        double blueDiff = Math.abs(colorToCheck.getBlue() - targetColor.getBlue());
        double totalDiff = redDiff + greenDiff + blueDiff;
        return totalDiff <= threshold;
    }

}
