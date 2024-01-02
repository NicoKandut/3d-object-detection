package nicok.thesis.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import static nicok.thesis.config.Constants.OPENCV_DLL;

public class HelloApplication extends Application {

    static {
        nu.pattern.OpenCV.loadLocally();
        System.load(OPENCV_DLL);
    }

    @Override
    public void start(Stage stage) throws IOException {
        final var fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        final var scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();


    }

    public static void main(String[] args) {
        launch();
    }
}