package com.stimart;

import com.stimart.Controller.PaintController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("main-view.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root);

        scene.setOnKeyPressed(event -> {
            PaintController controller = loader.getController();
            controller.checkShortcutMode(event);
        });

        primaryStage.setTitle("Paint App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
