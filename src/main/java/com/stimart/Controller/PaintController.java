package com.stimart.Controller;

import com.stimart.Class.Shortcut;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Controller class for managing the paint application.
 */
public class PaintController {

    @FXML
    private Canvas canvas;

    private GraphicsContext gc;

    private String lastMode = "";

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);

        // Use Timeline to check the Shortcut mode every 100ms
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> checkShortcutMode()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void checkShortcutMode() {
        Shortcut shortcut = Shortcut.getInstance();
        if (!lastMode.equals(shortcut.mode)) {
            lastMode = shortcut.mode;
            switch (lastMode) {
                case "pen":
                    usePen();
                    break;
                case "eraser":
                    useEraser();
                    break;
                default:
                    break;
            }
        }
    }

    private void onMousePressed(MouseEvent event) {
        gc.beginPath();
        gc.moveTo(event.getX(), event.getY());
        gc.stroke();
    }

    private void onMouseDragged(MouseEvent event) {
        gc.lineTo(event.getX(), event.getY());
        gc.stroke();
    }

    @FXML
    private void usePen() {
        gc.setStroke(Color.BLACK);
        canvas.setCursor(Cursor.DEFAULT);
        setPenSize(1);
    }

    @FXML
    private void useEraser() {
        gc.setStroke(Color.WHITE);
        canvas.setCursor(createEraserCursor());
        setPenSize(10);
    }

    private void setPenSize(double size) {
        gc.setLineWidth(size);
    }

    private Cursor createEraserCursor() {
        int size = 16;
        Canvas cursorCanvas = new Canvas(size, size);
        GraphicsContext cursorGc = cursorCanvas.getGraphicsContext2D();
        cursorGc.setFill(Color.LIGHTGRAY);
        cursorGc.fillRect(0, 0, size, size);
        return new ImageCursor(cursorCanvas.snapshot(null, null), size / 2, size / 2);
    }
}
