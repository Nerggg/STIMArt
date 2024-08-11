package com.stimart.Controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class PaintController {

    @FXML
    private Canvas canvas;

    private GraphicsContext gc;
    private boolean isEraserActive = false;

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
    }

    private void onMousePressed(MouseEvent event) {
        if (isEraserActive) {
            gc.clearRect(event.getX(), event.getY(), 10, 10);
        } else {
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            gc.stroke();
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (isEraserActive) {
            gc.clearRect(event.getX(), event.getY(), 10, 10);
        } else {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
        }
    }

    @FXML
    private void usePen() {
        isEraserActive = false;
        gc.setStroke(Color.BLACK);
    }

    @FXML
    private void useEraser() {
        isEraserActive = true;
    }
}
