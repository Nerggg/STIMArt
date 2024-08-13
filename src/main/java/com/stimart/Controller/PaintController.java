package com.stimart.Controller;

import com.stimart.Class.Shortcut;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class PaintController {

    @FXML
    private Canvas canvas;
    @FXML
    private Canvas topCanvas;

    @FXML
    private ColorPicker colorPicker;

    private GraphicsContext gcBottom;
    private GraphicsContext gcTop;

    private String lastMode = "";

    private double startX, startY, endX, endY;

    @FXML
    public void initialize() {
        gcBottom = canvas.getGraphicsContext2D();
        gcBottom.setFill(Color.WHITE);
        gcBottom.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gcBottom.setStroke(Color.BLACK);
        gcBottom.setLineWidth(2);

        gcTop = topCanvas.getGraphicsContext2D();

        // Set the initial color in the ColorPicker
        colorPicker.setValue(Color.BLACK);

        topCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        topCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);

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
                case "select":
                    useSelect();
                    break;
                default:
                    break;
            }
        }
    }

    private void onMousePressed(MouseEvent event) {
        if (lastMode.equals("select")) {
            startX = event.getX();
            startY = event.getY();
            gcTop.clearRect(0, 0, topCanvas.getWidth(), topCanvas.getHeight());
        }
        else {
            gcBottom.beginPath();
            gcBottom.moveTo(event.getX(), event.getY());
            gcBottom.stroke();
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (lastMode.equals("select")) {
            endX = event.getX();
            endY = event.getY();
            drawSelectBox(gcTop);
        }
        else {
            gcBottom.lineTo(event.getX(), event.getY());
            gcBottom.stroke();
        }
    }

    @FXML
    private void usePen() {
        lastMode = "pen";
        gcBottom.setStroke(colorPicker.getValue());
        topCanvas.setCursor(Cursor.DEFAULT);
        setPenSize(1);
    }

    @FXML
    private void useEraser() {
        lastMode = "eraser";
        gcBottom.setStroke(Color.WHITE);
        topCanvas.setCursor(createEraserCursor());
        setPenSize(10);
    }

    @FXML
    private void useSelect() {
        lastMode = "select";
        topCanvas.setCursor(Cursor.CROSSHAIR);
    }

    @FXML
    private void changePenColor() {
        if ("pen".equals(lastMode)) {
            gcBottom.setStroke(colorPicker.getValue());
        }
    }

    private void setPenSize(double size) {
        gcBottom.setLineWidth(size);
    }

    private Cursor createEraserCursor() {
        int size = 16;
        Canvas cursorCanvas = new Canvas(size, size);
        GraphicsContext cursorGc = cursorCanvas.getGraphicsContext2D();
        cursorGc.setFill(Color.LIGHTGRAY);
        cursorGc.fillRect(0, 0, size, size);
        return new ImageCursor(cursorCanvas.snapshot(null, null), size / 2, size / 2);
    }

    private void drawSelectBox(GraphicsContext gc) {
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);

        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setStroke(Color.BLUE);
        gc.setLineDashes(6);
        gc.strokeRect(x, y, width, height);
    }
}
