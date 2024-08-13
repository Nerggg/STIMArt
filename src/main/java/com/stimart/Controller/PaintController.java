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

    private GraphicsContext gc;
    private GraphicsContext gcTop;

    private String lastMode = "";
    private double startX, startY, endX, endY;

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        gcTop = topCanvas.getGraphicsContext2D();

        // Set the initial color in the ColorPicker
        colorPicker.setValue(Color.BLACK);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);

        topCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onTopCanvasMousePressed);
        topCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onTopCanvasMouseDragged);
        topCanvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onTopCanvasMouseReleased);

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
        if ("pen".equals(lastMode)) {
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            gc.stroke();
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if ("pen".equals(lastMode)) {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
        }
    }

    private void onTopCanvasMousePressed(MouseEvent event) {
        if ("select".equals(lastMode)) {
            startX = event.getX();
            startY = event.getY();
            gcTop.clearRect(0, 0, topCanvas.getWidth(), topCanvas.getHeight());
        }
        else {
            onMousePressed(event);
        }
    }

    private void onTopCanvasMouseDragged(MouseEvent event) {
        if ("select".equals(lastMode)) {
            endX = event.getX();
            endY = event.getY();
            drawSelectBox();
        }
        else {
            onMouseDragged(event);
        }
    }

    private void onTopCanvasMouseReleased(MouseEvent event) {
        if ("select".equals(lastMode)) {
            endX = event.getX();
            endY = event.getY();
            drawSelectBox();
            gcTop.clearRect(0, 0, topCanvas.getWidth(), topCanvas.getHeight());
        }
    }

    @FXML
    private void usePen() {
        lastMode = "pen";
        gcTop.setStroke(colorPicker.getValue());
        topCanvas.setCursor(Cursor.DEFAULT);
        setPenSize(1);
    }

    @FXML
    private void useEraser() {
        lastMode = "eraser";
        gcTop.setStroke(Color.WHITE);
        topCanvas.setCursor(createEraserCursor());
        setPenSize(10);
        System.out.println("eraserrr");
    }

    @FXML
    private void useSelect() {
        lastMode = "select";
        topCanvas.setCursor(Cursor.CROSSHAIR);
    }

    @FXML
    private void changePenColor() {
        if ("pen".equals(lastMode)) {
            gcTop.setStroke(colorPicker.getValue());
        }
    }

    private void setPenSize(double size) {
        gcTop.setLineWidth(size);
    }

    private Cursor createEraserCursor() {
        int size = 16;
        Canvas cursorCanvas = new Canvas(size, size);
        GraphicsContext cursorGc = cursorCanvas.getGraphicsContext2D();
        cursorGc.setFill(Color.LIGHTGRAY);
        cursorGc.fillRect(0, 0, size, size);
        return new ImageCursor(cursorCanvas.snapshot(null, null), size / 2, size / 2);
    }

    private void drawSelectBox() {
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);

        gcTop.clearRect(0, 0, topCanvas.getWidth(), topCanvas.getHeight());
        gcTop.setStroke(Color.BLUE);
        gcTop.setLineDashes(6);
        gcTop.strokeRect(x, y, width, height);
    }
}
