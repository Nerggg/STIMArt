package com.stimart.Class;

import java.util.ArrayList;

public class AppState {
    public ArrayList<LineSegment> lineSegments;
    public ArrayList<ExternalImages> externalImages;

    public AppState(ArrayList<LineSegment> lineSegments, ArrayList<ExternalImages> externalImages) {
        this.lineSegments = lineSegments;
        this.externalImages = externalImages;
    }
}
