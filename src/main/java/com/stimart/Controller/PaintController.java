package com.stimart.Controller;

import com.stimart.Class.LineSegment;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class PaintController {

    @FXML
    private Canvas bottomCanvas;
    @FXML
    private Canvas topCanvas;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Slider sizeSlider;
    @FXML
    private TextField sizeField;

    private GraphicsContext gcBottom;
    private GraphicsContext gcTop;

    private String lastMode = "";

    // selection
    private double selectStartX, selectStartY, selectEndX, selectEndY;
    private List<LineSegment> lineSegments = new ArrayList<>();
    private List<LineSegment> selectedSegments = new ArrayList<>();

    // drawing
    private double startX, startY, endX, endY;

    @FXML
    public void initialize() {
        gcBottom = bottomCanvas.getGraphicsContext2D();
        gcBottom.setFill(Color.WHITE);
        gcBottom.fillRect(0, 0, bottomCanvas.getWidth(), bottomCanvas.getHeight());

        gcTop = topCanvas.getGraphicsContext2D();

        colorPicker.setValue(Color.BLACK);

        topCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        topCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        topCanvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
        usePen();

        sizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            sizeField.setText(String.valueOf(newValue.intValue()));
        });

        sizeField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int value = Integer.parseInt(newValue);
                if (value >= sizeSlider.getMin() && value <= sizeSlider.getMax()) {
                    sizeSlider.setValue(value);
                } else {
                    sizeField.setText(oldValue);
                }
            } catch (NumberFormatException e) {
                sizeField.setText(oldValue);
            }
            if (lastMode.equals("eraser")) {
                topCanvas.setCursor(createEraserCursor());
                setPenSize(Double.valueOf(sizeField.getText()) + 9);
            }
            else {
                setPenSize(Double.valueOf(sizeField.getText()));
            }
        });
    }

    @FXML
    public void checkShortcutMode(KeyEvent event) {
        if (event.isControlDown() && event.getCode() == KeyCode.X) {
            System.out.println("cut");
        }
        else if (event.isControlDown() && event.getCode() == KeyCode.C) {
            System.out.println("copy");
        }
        else if (event.isControlDown() && event.getCode() == KeyCode.P) {
            System.out.println("paste");
        }
        else if (event.getCode() == KeyCode.M) {
            useMove();
        }
        else if (event.getCode() == KeyCode.P) {
            usePen();
        }
        else if (event.getCode() == KeyCode.E) {
            useEraser();
        }
        else if (event.getCode() == KeyCode.S && !lastMode.equals("select")) {
            useSelect();
        }
        else if (event.getCode() == KeyCode.S && lastMode.equals("select")) {
            resetSelectBox(gcTop);
        }
        else if (event.getCode() == KeyCode.DELETE) {
            lineSegments.removeAll(selectedSegments);
            drawAll(gcBottom);
            resetSelectBox(gcTop);
        }
    }

    private void onMousePressed(MouseEvent event) {
        if (lastMode.equals("select")) {
            selectStartX = event.getX();
            selectStartY = event.getY();
            gcTop.clearRect(0, 0, topCanvas.getWidth(), topCanvas.getHeight());
        }
        else {
            startX = event.getX();
            startY = event.getY();
            gcBottom.beginPath();
            gcBottom.moveTo(startX, startY);
            gcBottom.stroke();
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (lastMode.equals("select")) {
            selectEndX = event.getX();
            selectEndY = event.getY();
            drawSelectBox(gcTop);
        }
        else if (lastMode.equals("move")) {
            double deltaX = event.getX() - startX;
            double deltaY = event.getY() - startY;
            selectStartX += deltaX;
            selectEndX += deltaX;
            selectStartY += deltaY;
            selectEndY += deltaY;

            for (LineSegment segment : selectedSegments) {
                segment.move(deltaX, deltaY);
            }

            startX = event.getX();
            startY = event.getY();
            drawAll(gcBottom);
            drawSelectBox(gcTop);
        }
        else {
            endX = event.getX();
            endY = event.getY();
            gcBottom.lineTo(endX, endY);
            gcBottom.stroke();
            lineSegments.add(new LineSegment(startX, startY, endX, endY, colorPicker.getValue(), gcBottom.getLineWidth()));
            startX = endX;
            startY = endY;
        }
    }

    private void onMouseReleased(MouseEvent event) {
        if (lastMode.equals("select")) {
            selectSegments(Math.min(selectStartX, selectEndX), Math.min(selectStartY, selectEndY), Math.abs(selectEndX - selectStartX), Math.abs(selectEndY - selectStartY));
        }
    }

    @FXML
    private void useMove() {
        lastMode = "move";
        topCanvas.setCursor(Cursor.MOVE);
    }

    @FXML
    private void usePen() {
        setPenSize(Double.valueOf(sizeField.getText()));
        lastMode = "pen";
        gcBottom.setStroke(colorPicker.getValue());
        topCanvas.setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void useEraser() {
        setPenSize(Double.valueOf(sizeField.getText()) + 9);
        lastMode = "eraser";
        gcBottom.setStroke(Color.WHITE);
        topCanvas.setCursor(createEraserCursor());
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
        else if (!selectedSegments.isEmpty()) {
            for (LineSegment segment : selectedSegments) {
                segment.color = colorPicker.getValue();
            }
            drawAll(gcBottom);
        }
    }

    private void setPenSize(double size) {
        gcBottom.setLineWidth(size);
    }

    private Cursor createEraserCursor() {
        int size = (int)gcBottom.getLineWidth() + 9;
        Canvas cursorCanvas = new Canvas(size, size);
        GraphicsContext cursorGc = cursorCanvas.getGraphicsContext2D();
        cursorGc.setFill(Color.LIGHTGRAY);
        cursorGc.fillRect(0, 0, size, size);
        return new ImageCursor(cursorCanvas.snapshot(null, null), size / 2, size / 2);
    }

    private void drawSelectBox(GraphicsContext gc) {
        double x = Math.min(selectStartX, selectEndX);
        double y = Math.min(selectStartY, selectEndY);
        double width = Math.abs(selectEndX - selectStartX);
        double height = Math.abs(selectEndY - selectStartY);

        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setStroke(Color.BLUE);
        gc.setLineDashes(6);
        gc.strokeRect(x, y, width, height);
    }

    private void resetSelectBox(GraphicsContext gc) {
        gcTop.clearRect(0, 0, topCanvas.getWidth(), topCanvas.getHeight());
        selectStartX = 0;
        selectEndX = 0;
        selectStartY = 0;
        selectEndY = 0;
    }

    private void drawAll(GraphicsContext gc) {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gcBottom.setFill(Color.WHITE);
        gcBottom.fillRect(0, 0, bottomCanvas.getWidth(), bottomCanvas.getHeight());
        for (LineSegment segment : lineSegments) {
            gc.setStroke(segment.color);
            gc.setLineWidth(segment.size);
            gc.strokeLine(segment.startX, segment.startY, segment.endX, segment.endY);
        }
    }

    private void selectSegments(double boxX, double boxY, double boxWidth, double boxHeight) {
        selectedSegments.clear();
        for (LineSegment segment : lineSegments) {
            if (segment.isWithin(boxX, boxY, boxWidth, boxHeight)) {
                selectedSegments.add(segment);
            }
        }
    }
}