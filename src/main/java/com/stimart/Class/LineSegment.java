package com.stimart.Class;

import javafx.scene.paint.Color;

public class LineSegment {
    public double startX, startY, endX, endY;
    public Color color;
    public double size;

    public LineSegment(double startX, double startY, double endX, double endY, Color color, double size) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.color = color;
        this.size = size;
    }

    public boolean isWithin(double x, double y, double width, double height) {
        return (Math.min(startX, endX) >= x && Math.max(startX, endX) <= x + width &&
                Math.min(startY, endY) >= y && Math.max(startY, endY) <= y + height);
    }

    public void move(double deltaX, double deltaY) {
        startX += deltaX;
        startY += deltaY;
        endX += deltaX;
        endY += deltaY;
    }

    public boolean overlaps(LineSegment other) {
        double thisMinX = Math.min(this.startX, this.endX);
        double thisMaxX = Math.max(this.startX, this.endX);
        double thisMinY = Math.min(this.startY, this.endY);
        double thisMaxY = Math.max(this.startY, this.endY);

        double otherMinX = Math.min(other.startX, other.endX);
        double otherMaxX = Math.max(other.startX, other.endX);
        double otherMinY = Math.min(other.startY, other.endY);
        double otherMaxY = Math.max(other.startY, other.endY);

        // Check if bounding boxes intersect
        return (thisMinX <= otherMaxX && thisMaxX >= otherMinX &&
                thisMinY <= otherMaxY && thisMaxY >= otherMinY);
    }
}
