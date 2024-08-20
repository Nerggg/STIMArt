package com.stimart.Class;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class ColorDepth {
    public static WritableImage reduceColorDepth(ExternalImages ei, int targetDepth) {
        int width = (int) ei.image.getWidth();
        int height = (int) ei.image.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = ei.originalImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixelReader.getArgb(x, y);

                int alpha = (argb >> 24) & 0xff;
                int red = (argb >> 16) & 0xff;
                int green = (argb >> 8) & 0xff;
                int blue = argb & 0xff;

                switch (targetDepth) {
                    case 24:
                        break;
                    case 20:
                        red = (red >> 1) << 1;
                        green = (green >> 1) << 1;
                        blue = (blue >> 2) << 2;
                        break;
                    case 16:
                        red = (red >> 3) << 3;
                        green = (green >> 2) << 2;
                        blue = (blue >> 3) << 3;
                        break;
                    case 12:
                        red = (red >> 4) << 4;
                        green = (green >> 4) << 4;
                        blue = (blue >> 4) << 4;
                        break;
                    case 8:
                        red = (red >> 5) << 5;
                        green = (green >> 5) << 5;
                        blue = (blue >> 6) << 6;
                        break;
                    case 4:
                        red = (red >> 6) << 6;
                        green = (green >> 6) << 6;
                        blue = (blue >> 7) << 7;
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported target depth: " + targetDepth);
                }

                int newArgb = (alpha << 24) | (red << 16) | (green << 8) | blue;

                pixelWriter.setArgb(x, y, newArgb);
            }
        }

        return writableImage;
    }

}
