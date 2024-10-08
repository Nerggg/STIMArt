package com.stimart.Controller;

import com.stimart.Class.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class PaintController {

    @FXML
    private Canvas bottomCanvas;
    @FXML
    private Canvas topCanvas;
    @FXML
    private Canvas imageCanvas;
    @FXML
    private Canvas whiteCanvas;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Button upwardButton;
    @FXML
    private Button downwardButton;
    @FXML
    private Slider sizeSlider;
    @FXML
    private TextField sizeField;
    @FXML
    private Slider blurSlider;
    @FXML
    private Label blurLevel;
    @FXML
    private Slider colorDepthSlider;
    @FXML
    private Label colorDepthLabel;
    @FXML
    private Button segmentButton;
    @FXML
    private Label segmentThresholdLabel;
    @FXML
    private TextField segmentThreshold;

    private GraphicsContext gcBottom;
    private GraphicsContext gcTop;
    private GraphicsContext gcImage;

    private String lastMode = "";

    // selection
    private double selectStartX, selectStartY, selectEndX, selectEndY;
    private double formerSelectStartX, formerSelectStartY, formerSelectEndX, formerSelectEndY;
    private ArrayList<LineSegment> lineSegments = new ArrayList<>();
    private ArrayList<LineSegment> selectedSegments = new ArrayList<>();
    private ArrayList<LineSegment> eraseSegments = new ArrayList<>();
    private ArrayList<LineSegment> pasteTemp = new ArrayList<>();

    // drawing
    private double startX, startY, endX, endY;

    // external images
    private ArrayList<ExternalImages> externalImages = new ArrayList<>();
    private int selectedImage = -1;

    // states
    private StateArray stateArray = new StateArray();

    @FXML
    public void initialize() {
        gcBottom = whiteCanvas.getGraphicsContext2D();
        gcBottom.setFill(Color.WHITE);
        gcBottom.fillRect(0, 0, bottomCanvas.getWidth(), bottomCanvas.getHeight());

        gcBottom = bottomCanvas.getGraphicsContext2D();
        gcTop = topCanvas.getGraphicsContext2D();
        gcImage = imageCanvas.getGraphicsContext2D();

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

        blurSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedImage != -1) {
                externalImages.get(selectedImage).originalImage = externalImages.get(selectedImage).veryOriginalImage;
                if (externalImages.get(selectedImage).depthReduced) {
                    externalImages.get(selectedImage).originalImage = ColorDepth.reduceColorDepth(externalImages.get(selectedImage), externalImages.get(selectedImage).colorDepth);
                }

                externalImages.get(selectedImage).blurred = newValue.intValue() != 0;

                blurLevel.setText("Blur Level: " + newValue.intValue());
                externalImages.get(selectedImage).blur = newValue.intValue();
                if (newValue.intValue() == 0) {
                    externalImages.get(selectedImage).image = ImageBlurring.blurImageCaller(externalImages.get(selectedImage), 0);
                }
                else {
                    externalImages.get(selectedImage).image = ImageBlurring.blurImageCaller(externalImages.get(selectedImage), 11 - newValue.intValue());
                }
                drawAllImages(gcImage);
            }
        });

        blurSlider.setOnMouseReleased(event -> {
            stateArray.addState(lineSegments, externalImages);
        });

        colorDepthSlider.setValue(24);
        colorDepthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedImage != -1) {
                externalImages.get(selectedImage).originalImage = externalImages.get(selectedImage).veryOriginalImage;
                if (externalImages.get(selectedImage).blurred) {
                    externalImages.get(selectedImage).originalImage = ImageBlurring.blurImageCaller(externalImages.get(selectedImage), 11 - externalImages.get(selectedImage).blur);
                }

                if (newValue.intValue() % 4 == 0) {
                    externalImages.get(selectedImage).depthReduced = newValue.intValue() != 24;

                    colorDepthLabel.setText("Color Depth: " + newValue.intValue() + " bit");
                    externalImages.get(selectedImage).colorDepth = newValue.intValue();
                    externalImages.get(selectedImage).image = ColorDepth.reduceColorDepth(externalImages.get(selectedImage), newValue.intValue());
                }
                drawAllImages(gcImage);
            }
        });

        colorDepthSlider.setOnMouseReleased(event -> {
            stateArray.addState(lineSegments, externalImages);
        });

        Pattern validPattern = Pattern.compile("([0-2](\\.\\d{0,1})?|3(\\.0?)?)");

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (validPattern.matcher(newText).matches()) {
                return change;
            }
            return null;
        };

        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        segmentThreshold.setTextFormatter(textFormatter);

        colorDepthSlider.setVisible(false);
        colorDepthLabel.setVisible(false);
        blurSlider.setVisible(false);
        blurLevel.setVisible(false);
        upwardButton.setVisible(false);
        downwardButton.setVisible(false);
        segmentButton.setVisible(false);
        segmentThresholdLabel.setVisible(false);
        segmentThreshold.setVisible(false);
    }

    @FXML
    public void checkShortcutMode(KeyEvent event) {
        if (event.isControlDown() && event.getCode() == KeyCode.X) {
            copyToClipboard();
            lineSegments.removeAll(selectedSegments);
            drawAll(gcBottom);
            resetSelectBox(gcTop);
        }
        else if (event.isControlDown() && event.getCode() == KeyCode.C) {
            copyToClipboard();
        }
        else if (event.isControlDown() && event.getCode() == KeyCode.V) {
            pasteFromClipboard(gcBottom);
        }
        else if (event.isControlDown() && event.getCode() == KeyCode.O) {
            openImage();
            stateArray.addState(lineSegments, externalImages);
        }
        else if (event.isControlDown() && event.getCode() == KeyCode.S) {
            saveCanvasToFile();
        }
        else if (event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.Z) {
            stateArray.redo();
            lineSegments = stateArray.states.get(stateArray.active).lineSegments;
            externalImages = stateArray.states.get(stateArray.active).externalImages;
            drawAll(gcBottom);
            drawAllImages(gcImage);
            resetSelectBox(gcTop);
        }
        else if (event.isControlDown() && event.getCode() == KeyCode.Z) {
            stateArray.undo();
            lineSegments = stateArray.states.get(stateArray.active).lineSegments;
            externalImages = stateArray.states.get(stateArray.active).externalImages;
            drawAll(gcBottom);
            drawAllImages(gcImage);
            resetSelectBox(gcTop);
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
            if (selectedImage == -1) {
                lineSegments.removeAll(selectedSegments);
                drawAll(gcBottom);
            }
            else {
                externalImages.remove(selectedImage);
                drawAllImages(gcImage);
            }
            stateArray.addState(lineSegments, externalImages);
            resetSelectBox(gcTop);
        }
    }

    private void onMousePressed(MouseEvent event) {
        if (lastMode.equals("select")) {
            selectStartX = event.getX();
            selectStartY = event.getY();
            gcTop.clearRect(0, 0, topCanvas.getWidth(), topCanvas.getHeight());

            for (int i = externalImages.size() - 1; i >= 0; i--) {
                if (isImageClicked(event.getX(), event.getY(), externalImages.get(i))) {
                    selectStartX = externalImages.get(i).imageX;
                    selectStartY = externalImages.get(i).imageY;
                    selectEndX = externalImages.get(i).image.getWidth() + externalImages.get(i).imageX;
                    selectEndY = externalImages.get(i).image.getHeight() + externalImages.get(i).imageY;
                    drawSelectBox(gcTop);
                    selectedImage = i;
                    blurLevel.setText("Blur Level: " + externalImages.get(i).blur);
                    blurSlider.setValue(externalImages.get(i).blur);

                    colorDepthSlider.setVisible(true);
                    colorDepthLabel.setVisible(true);
                    blurSlider.setVisible(true);
                    blurLevel.setVisible(true);
                    upwardButton.setVisible(true);
                    downwardButton.setVisible(true);
                    segmentButton.setVisible(true);
                    segmentThresholdLabel.setVisible(true);
                    segmentThreshold.setVisible(true);
                    break;
                }
            }
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
            if (selectedImage == -1) {
                for (LineSegment segment : selectedSegments) {
                    segment.move(deltaX, deltaY);
                }
            }
            else {
                externalImages.get(selectedImage).imageX += deltaX;
                externalImages.get(selectedImage).imageY += deltaY;
            }
            startX = event.getX();
            startY = event.getY();
            drawAll(gcBottom);
            drawAllImages(gcImage);
            drawSelectBox(gcTop);
        }
        else {
            endX = event.getX();
            endY = event.getY();
            gcBottom.lineTo(endX, endY);
            gcBottom.stroke();
            if (lastMode.equals("eraser")) {
                eraseSegments.add(new LineSegment(startX, startY, endX, endY, Color.WHITE, gcBottom.getLineWidth()));
            }
            else {
                lineSegments.add(new LineSegment(startX, startY, endX, endY, colorPicker.getValue(), gcBottom.getLineWidth()));
            }
            startX = endX;
            startY = endY;
            lineSegments = getNonOverlappingSegments(lineSegments, eraseSegments);
            eraseSegments.clear();
            drawAll(gcBottom);
            drawAllImages(gcImage);
            drawSelectBox(gcTop);
        }
    }

    private void onMouseReleased(MouseEvent event) {
        if (lastMode.equals("select")) {
            selectSegments(Math.min(selectStartX, selectEndX), Math.min(selectStartY, selectEndY), Math.abs(selectEndX - selectStartX), Math.abs(selectEndY - selectStartY));
        }
        eraseSegments.clear();
        stateArray.addState(lineSegments, externalImages);
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
        if (selectedImage != -1) {
            selectedImage = -1;

            colorDepthSlider.setVisible(false);
            colorDepthLabel.setVisible(false);
            blurSlider.setVisible(false);
            blurLevel.setVisible(false);
            upwardButton.setVisible(false);
            downwardButton.setVisible(false);
            segmentButton.setVisible(false);
            segmentThresholdLabel.setVisible(false);
            segmentThreshold.setVisible(false);
        }

        gcTop.clearRect(0, 0, topCanvas.getWidth(), topCanvas.getHeight());
        selectStartX = 0;
        selectEndX = 0;
        selectStartY = 0;
        selectEndY = 0;
//        lineSegments.addAll(pasteTemp);

        blurSlider.setValue(0);
        blurLevel.setText("Blur Level: 0");
    }

    private void drawAll(GraphicsContext gc) {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        for (LineSegment segment : lineSegments) {
            gc.setStroke(segment.color);
            gc.setLineWidth(segment.size);
            gc.strokeLine(segment.startX, segment.startY, segment.endX, segment.endY);
        }
//        for (LineSegment segment : pasteTemp) {
//            gc.setStroke(segment.color);
//            gc.setLineWidth(segment.size);
//            gc.strokeLine(segment.startX, segment.startY, segment.endX, segment.endY);
//        }
    }

    private void selectSegments(double boxX, double boxY, double boxWidth, double boxHeight) {
        selectedSegments.clear();
        for (LineSegment segment : lineSegments) {
            if (segment.isWithin(boxX, boxY, boxWidth, boxHeight)) {
                selectedSegments.add(segment);
            }
        }
    }

    private ArrayList<LineSegment> getNonOverlappingSegments(ArrayList<LineSegment> A, ArrayList<LineSegment> B) {
        ArrayList<LineSegment> nonOverlappingSegments = new ArrayList<>();

        for (LineSegment segmentA : A) {
            boolean isOverlapping = false;
            for (LineSegment segmentB : B) {
                if (segmentA.overlaps(segmentB)) {
                    isOverlapping = true;
                    break;
                }
            }
            if (!isOverlapping) {
                nonOverlappingSegments.add(segmentA);
            }
        }

        return nonOverlappingSegments;
    }

    private void copyToClipboard() {
        formerSelectStartX = selectStartX;
        formerSelectStartY = selectStartY;
        formerSelectEndX = selectEndX;
        formerSelectEndY = selectEndY;
        StringBuilder sb = new StringBuilder();
        for (LineSegment segment : selectedSegments) {
            sb.append(String.format("%.2f, %.2f, %.2f, %.2f, %s, %.2f%n",
                    segment.startX, segment.startY, segment.endX, segment.endY,
                    segment.color.toString(), segment.size));
        }

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        clipboard.setContent(content);
    }

    private void pasteFromClipboard(GraphicsContext gc) {
        pasteTemp.clear();
        selectedSegments.clear();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        String clipboardContent = clipboard.getString();
        double minStartX = Double.MAX_VALUE;
        double minStartY = Double.MAX_VALUE;

        if (clipboardContent != null) {
            String[] lines = clipboardContent.split("\n");
            for (String line : lines) {
                LineSegment segment = parseLineSegment(line);
                pasteTemp.add(segment);
                if (segment.startX < minStartX) {
                    minStartX = segment.startX;
                }
                if (segment.startY < minStartY) {
                    minStartY = segment.startY;
                }
            }
        }

            for (LineSegment segment : pasteTemp) {
                try {
                    segment.endX -= minStartX;
                    segment.endY -= minStartY;
                    segment.startX -= minStartX;
                    segment.startY -= minStartY;
                    selectEndX = formerSelectEndX - formerSelectStartX;
                    selectEndY = formerSelectEndY - formerSelectStartY;
                    selectStartX = 0;
                    selectStartY = 0;
                    lineSegments.add(segment);
                    drawSelectBox(gcTop);
                    drawAll(gcBottom);
                    selectedSegments.add(segment);
                } catch (Exception e) {
                    System.err.println("Failed to parse line");
                }
            }
        }

    private LineSegment parseLineSegment(String line) {
        String[] parts = line.split(", ");

        try {
            return new LineSegment(Double.valueOf(parts[0]), Double.valueOf(parts[1]), Double.valueOf(parts[2]), Double.valueOf(parts[3]), Color.web(parts[4]), Double.valueOf(parts[5]));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void openImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) bottomCanvas.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            externalImages.add(new ExternalImages(image, 0, 0));
            selectStartX = 0;
            selectStartY = 0;
            selectEndX = externalImages.getLast().image.getWidth();
            selectEndY = externalImages.getLast().image.getHeight();
            drawSelectBox(gcTop);
            drawAllImages(gcImage);
            selectedImage = externalImages.size() - 1;

            colorDepthSlider.setVisible(true);
            colorDepthLabel.setVisible(true);
            blurSlider.setVisible(true);
            blurLevel.setVisible(true);
            upwardButton.setVisible(true);
            downwardButton.setVisible(true);
            segmentButton.setVisible(true);
            segmentThresholdLabel.setVisible(true);
            segmentThreshold.setVisible(true);

        }
    }

    private void drawAllImages(GraphicsContext gc) {
        gc.clearRect(0, 0, imageCanvas.getWidth(), imageCanvas.getHeight());
        for (ExternalImages ei : externalImages) {
            gc.drawImage(ei.image, ei.imageX, ei.imageY, ei.image.getWidth(), ei.image.getHeight());
        }
    }

    private boolean isImageClicked(double x, double y, ExternalImages ei) {
        double imgX = ei.imageX;
        double imgY = ei.imageY;
        Image image = ei.image;
        return x >= imgX && x <= imgX + image.getWidth() &&
                y >= imgY && y <= imgY + image.getHeight();
    }

    @FXML
    private void upwardImage() {
        if (selectedImage != -1 && selectedImage != externalImages.size() - 1) {
            ExternalImages temp = externalImages.remove(selectedImage);
            selectedImage++;
            externalImages.add(selectedImage, temp);
            drawAllImages(gcImage);
            stateArray.addState(lineSegments, externalImages);
        }
    }

    @FXML
    private void downwardImage() {
        if (selectedImage != -1 && selectedImage != 0) {
            ExternalImages temp = externalImages.remove(selectedImage);
            selectedImage--;
            externalImages.add(selectedImage, temp);
            drawAllImages(gcImage);
            stateArray.addState(lineSegments, externalImages);
        }
    }

    public static boolean isCursorOverLine(LineSegment line, double cursorX, double cursorY) {
        double buffer = 5.0;
        double distance = pointToLineDistance(line.startX, line.startY, line.endX, line.endY, cursorX, cursorY);
        return distance <= buffer;
    }

    public static double pointToLineDistance(double x1, double y1, double x2, double y2, double px, double py) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = dot / lenSq;

        double xx, yy;

        if (param < 0 || (x1 == x2 && y1 == y2)) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        double dx = px - xx;
        double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @FXML
    private void segmentImage() {
        ImageSegmentation.segmentImage(externalImages, selectedImage, colorPicker.getValue(), Double.valueOf(segmentThreshold.getText()));
    }

    private void saveCanvasToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                Canvas saveImageCanvas = new Canvas(bottomCanvas.getWidth(), bottomCanvas.getHeight());
                GraphicsContext saveImageGC = saveImageCanvas.getGraphicsContext2D();
                saveImageGC.clearRect(0, 0, saveImageGC.getCanvas().getWidth(), saveImageGC.getCanvas().getHeight());
                for (ExternalImages ei : externalImages) {
                    saveImageGC.drawImage(ei.image, ei.imageX, ei.imageY, ei.image.getWidth(), ei.image.getHeight());
                }
                for (LineSegment segment : lineSegments) {
                    saveImageGC.setStroke(segment.color);
                    saveImageGC.setLineWidth(segment.size);
                    saveImageGC.strokeLine(segment.startX, segment.startY, segment.endX, segment.endY);
                }
                for (LineSegment segment : pasteTemp) {
                    saveImageGC.setStroke(segment.color);
                    saveImageGC.setLineWidth(segment.size);
                    saveImageGC.strokeLine(segment.startX, segment.startY, segment.endX, segment.endY);
                }

                WritableImage saveImage = new WritableImage((int) bottomCanvas.getWidth(), (int) bottomCanvas.getHeight());
                saveImageCanvas.snapshot(null, saveImage);

                ImageIO.write(SwingFXUtils.fromFXImage(saveImage, null), "png", file);
            } catch (IOException ex) {
                System.out.println("Failed to save image: " + ex.getMessage());
            }
        }
    }
}