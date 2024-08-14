package com.stimart.Class;

import javafx.scene.paint.Color;

public class LineSegment {
    public double startX, startY, endX, endY;
    public Color color;

    public LineSegment(double startX, double startY, double endX, double endY, Color color) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.color = color;
    }

    // Check if the line segment is within the specified rectangle
    public boolean isWithin(double x, double y, double width, double height) {
        return (Math.min(startX, endX) >= x && Math.max(startX, endX) <= x + width &&
                Math.min(startY, endY) >= y && Math.max(startY, endY) <= y + height);
    }

    // Move the line segment by the specified delta
    public void move(double deltaX, double deltaY) {
        startX += deltaX;
        startY += deltaY;
        endX += deltaX;
        endY += deltaY;
    }
}
