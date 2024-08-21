package com.stimart.Class;

import java.util.ArrayList;

public class StateArray {
    public ArrayList<AppState> states = new ArrayList<>();
    public int active = 0;

    public StateArray() {
        ArrayList<LineSegment> lineSegment = new ArrayList<>();
        ArrayList<ExternalImages> externalImages = new ArrayList<>();

        states.add(new AppState(lineSegment, externalImages));
    }

    public void undo() {
        if (active > 0) {
            active--;
        }
    }

    public void redo() {
        if (active < states.size() - 1) {
            active++;
        }
    }

    public void addState(AppState state) {
        active++;
        if (states.size() - 1 >= active) {
            deleteOnward(active);
        }
        states.add(state);

        if (states.get(active - 1).lineSegments.size() > 0) {
            states.get(active - 1).lineSegments.removeLast();
        }
    }

    public void deleteOnward(int idx) {
        int delAmount = states.size() - idx;
        for (int i = 0; i < delAmount; i++) {
            states.remove(idx);
        }
    }
}


