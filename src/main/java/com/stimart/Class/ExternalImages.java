package com.stimart.Class;

import javafx.scene.image.Image;

public class ExternalImages {

    public Image originalImage;
    public Image image;
    public double imageX, imageY;
    public int blur = 0;
    public int colorDepth = 24;


    public ExternalImages(Image image, double imageX, double imageY) {
        this.originalImage = image;
        this.image = image;
        this.imageX = imageX;
        this.imageY = imageY;
    }
}
