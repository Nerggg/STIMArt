package com.stimart;

import com.stimart.Class.Shortcut;
import com.stimart.Controller.PaintController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("main-view.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root);

        scene.setOnKeyPressed(event -> handleKeyPress(event));

        primaryStage.setTitle("Paint App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

//    private void handleKeyPress(KeyEvent event) {
//        Shortcut s = Shortcut.getInstance();
//        switch (event.getCode()) {
//            case P:
//                s.mode = "pen";
//                break;
//            case E:
//                s.mode = "eraser";
//                break;
//            default:
//                break;
//        }
//    }

    private void handleKeyPress(KeyEvent event) {
        Shortcut s = Shortcut.getInstance();
        if (event.getCode() == KeyCode.P) {
            s.mode = "pen";
        }
        else if (event.getCode() == KeyCode.E) {
            s.mode = "eraser";
        }
        else if (event.isControlDown() && event.getCode() == KeyCode.S) {
            // ntar buat fungsi save
            System.out.println("save");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
