package nicok.thesis.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import nicok.thesis.detection.Detection;

import java.io.File;

import static nicok.thesis.config.Constants.RESOURCE_DIR;

public class HelloController {

    public static final String DEFAULT_IMAGE = RESOURCE_DIR + "/training-a-puppy.jpg";
    public static final String DEFAULT_OUTPUT = RESOURCE_DIR + "/output.jpg";

    @FXML
    private TextField imagePath;

    @FXML
    private Button chooseImagePath;

    @FXML
    private Pane imagePane;

    @FXML
    private ImageView imageView;

    @FXML
    private Label statusBar;

    @FXML
    public void initialize() {
        imagePath.setText(DEFAULT_IMAGE);
        statusBar.setText("Click RUN to get metrics");
        showImage(DEFAULT_IMAGE);
    }

    private void showImage(String imagePath) {
        final var file = new File(imagePath);
        final var image = new Image(file.toURI().toString());
        imageView.setImage(image);
    }

    @FXML
    private void runDetection(ActionEvent event) {
        final var timings = Detection.run(DEFAULT_IMAGE, DEFAULT_OUTPUT);
        showImage(DEFAULT_OUTPUT);
        statusBar.setText(String.format(
                "Detection took %f ms (model: %f, blob: %f, forward: %f, outputs: %f, boxes: %f, draw: %f, save: %f)",
                timings.getMillisBetween("start", "end"),
                timings.getMillisBetween("model-start", "model-end"),
                timings.getMillisBetween("blob-start", "blob-end"),
                timings.getMillisBetween("forward-start", "forward-end"),
                timings.getMillisBetween("outputs-start", "outputs-end"),
                timings.getMillisBetween("boxes-start", "boxes-end"),
                timings.getMillisBetween("draw-start", "draw-end"),
                timings.getMillisBetween("save-start", "save-end")
        ));
    }

    @FXML
    private void resetDetection(ActionEvent event) {
        statusBar.setText("Click RUN to get metrics");
        showImage(DEFAULT_IMAGE);
    }
}