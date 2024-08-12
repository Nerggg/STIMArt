package com.stimart.Class;

public class Shortcut {
    private static Shortcut single_instance = null;

    public String mode;

    private Shortcut() {
        this.mode = "pen";
    }

    public static synchronized Shortcut getInstance() {
        if (single_instance == null) {
            single_instance = new Shortcut();
        }
        return single_instance;
    }
}
