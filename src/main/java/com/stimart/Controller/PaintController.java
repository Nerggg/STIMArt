package com.stimart.Controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

/**
 * Controller class for managing the paint application.
 */
public class PaintController {

    // Reference to the Canvas component in the FXML file
    @FXML
    private Canvas canvas;

    // GraphicsContext used to draw on the Canvas
    private GraphicsContext gc;

    // Boolean flag to track whether the eraser is active
    private boolean isEraserActive = false;

    /**
     * Initializes the controller class. This method is called after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D(); // Get the GraphicsContext from the Canvas
        gc.setFill(Color.WHITE); // Set the fill color to white
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Fill the entire canvas with white
        gc.setStroke(Color.BLACK); // Set the stroke color to black
        gc.setLineWidth(2); // Set the initial stroke width to 2

        // Add mouse event handlers to the Canvas for drawing and erasing
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
    }

    /**
     * Handles the mouse pressed event on the Canvas.
     *
     * @param event the MouseEvent
     */
    private void onMousePressed(MouseEvent event) {
        // Start a new path at the mouse position
        gc.beginPath();
        gc.moveTo(event.getX(), event.getY());
        gc.stroke();
    }

    /**
     * Handles the mouse dragged event on the Canvas.
     *
     * @param event the MouseEvent
     */
    private void onMouseDragged(MouseEvent event) {
        // Draw a line to the current mouse position
        gc.lineTo(event.getX(), event.getY());
        gc.stroke();
    }

    /**
     * Activates the pen tool, allowing the user to draw on the Canvas.
     */
    @FXML
    private void usePen() {
        isEraserActive = false; // Deactivate the eraser
        gc.setStroke(Color.BLACK); // Set the stroke color to black
        canvas.setCursor(Cursor.DEFAULT); // Set the default cursor
        setPenSize(5); // Set the default pen size
    }

    /**
     * Activates the eraser tool, allowing the user to erase on the Canvas by drawing with white ink.
     */
    @FXML
    private void useEraser() {
        isEraserActive = true; // Activate the eraser
        gc.setStroke(Color.WHITE); // Set the stroke color to white to mimic erasing
        canvas.setCursor(createEraserCursor()); // Set a custom rectangle cursor
        setPenSize(10); // Set the eraser size
    }

    /**
     * Sets the size of the pen.
     *
     * @param size the desired pen size
     */
    private void setPenSize(double size) {
        gc.setLineWidth(size); // Set the line width to the specified size
    }

    /**
     * Creates a custom eraser cursor.
     *
     * @return the custom cursor for eraser
     */
    private Cursor createEraserCursor() {
        // Option 1: Create a rectangle cursor using an image file
        // Ensure you have a 16x16 image named 'eraser.png' in your resources
        // Image eraserImage = new Image(getClass().getResourceAsStream("/eraser.png"));
        // return new ImageCursor(eraserImage, 8, 8); // Hotspot at the center

        // Option 2: Create a simple programmatic rectangle cursor (no image file needed)
        int size = 16;
        Canvas cursorCanvas = new Canvas(size, size);
        GraphicsContext cursorGc = cursorCanvas.getGraphicsContext2D();
        cursorGc.setFill(Color.LIGHTGRAY);
        cursorGc.fillRect(0, 0, size, size);
        return new ImageCursor(cursorCanvas.snapshot(null, null), size / 2, size / 2); // Centered hotspot
    }
}
