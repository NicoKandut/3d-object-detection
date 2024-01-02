package nicok.bac.yolo3d;

import org.tensorflow.TensorFlow;

public class Main {
    public static void main(String[] args) {
        System.out.println("Using TensorFlow " + TensorFlow.version());

        try(final var session = Yolo3d.createModel(32, 1, 7)) {

        }
    }
}