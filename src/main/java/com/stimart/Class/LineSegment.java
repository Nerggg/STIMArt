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

    public LineSegment(LineSegment other) {
        this.startX = other.startX;
        this.startY = other.startY;
        this.endX = other.endX;
        this.endY = other.endY;
        this.color = other.color;
        this.size = other.size;
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
        double minX1 = Math.min(this.startX, this.endX) - this.size / 2;
        double maxX1 = Math.max(this.startX, this.endX) + this.size / 2;
        double minY1 = Math.min(this.startY, this.endY) - this.size / 2;
        double maxY1 = Math.max(this.startY, this.endY) + this.size / 2;

        double minX2 = Math.min(other.startX, other.endX) - other.size / 2;
        double maxX2 = Math.max(other.startX, other.endX) + other.size / 2;
        double minY2 = Math.min(other.startY, other.endY) - other.size / 2;
        double maxY2 = Math.max(other.startY, other.endY) + other.size / 2;

        boolean overlapX = (minX1 <= maxX2 && maxX1 >= minX2);
        boolean overlapY = (minY1 <= maxY2 && maxY1 >= minY2);

        return overlapX && overlapY;
    }
}
