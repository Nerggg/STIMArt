package com.stimart.Class;

import javafx.scene.image.Image;

public class ExternalImages {

    public Image veryOriginalImage;
    public Image originalImage;
    public Image image;
    public double imageX, imageY;
    public int blur = 0;
    public int colorDepth = 24;
    public boolean blurred = false;
    public boolean depthReduced = false;

    public ExternalImages(Image image, double imageX, double imageY) {
        this.veryOriginalImage = image;
        this.originalImage = image;
        this.image = image;
        this.imageX = imageX;
        this.imageY = imageY;
    }

    public ExternalImages(ExternalImages other) {
        this.veryOriginalImage = other.image;
        this.originalImage = other.image;
        this.image = other.image;
        this.imageX = other.imageX;
        this.imageY = other.imageY;
        this.blur = other.blur;
        this.colorDepth = other.colorDepth;
        this.blurred = other.blurred;
        this.depthReduced = other.depthReduced;
    }
}
