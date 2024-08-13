package com.stimart;

import com.stimart.Class.Shortcut;
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

    private void handleKeyPress(KeyEvent event) {
        Shortcut s = Shortcut.getInstance();
        if (event.isControlDown() && event.getCode() == KeyCode.X) {
            System.out.println("cut");
        }
        else if (event.isControlDown() && event.getCode() == KeyCode.C) {
            System.out.println("copy");
        }
        else if (event.isControlDown() && event.getCode() == KeyCode.P) {
            System.out.println("paste");
        }
        else if (event.getCode() == KeyCode.P) {
            s.mode = "pen";
        }
        else if (event.getCode() == KeyCode.E) {
            s.mode = "eraser";
        }
        else if (event.getCode() == KeyCode.S) {
            s.mode = "select";
        }
        else if (event.getCode() == KeyCode.DELETE) {
            System.out.println("delete");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
