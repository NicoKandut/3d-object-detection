package nicok.bac.yolo3d;

import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.network.Yolo3dNetwork;
import nicok.bac.yolo3d.scanner.Scanner;
import org.tensorflow.TensorFlow;

import java.io.IOException;

public class Main {

    public static final String REPOSITORY_PATH = "C:/src/bac";
    public static final String SAVED_MODEL_PATH = REPOSITORY_PATH + "/yolo-3d-poc/saved_model";
    public static final String VOX_PATH = REPOSITORY_PATH + "/dataset-3d-minecraft/test_0.vox";

    public static void main(String[] args) throws IOException {
        System.out.printf("Using TensorFlow %s\n", TensorFlow.version());

        final var inputFile = InputFileProvider.get(VOX_PATH);
        final var network = new Yolo3dNetwork(SAVED_MODEL_PATH);
        final var scanner = new Scanner(network);
        final var result = scanner.scan(inputFile);
        final var filter = new BoxFilter(0.6, 0.5);
        final var boxes = filter.filter(result.objects());

        System.out.printf("Found %d objects.\n", boxes.size());

        for(final var box : boxes) {
            System.out.printf("  - %s\n", box);
        }
    }
}