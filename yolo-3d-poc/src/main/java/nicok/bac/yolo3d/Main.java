package nicok.bac.yolo3d;

import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.scanner.Scanner;
import nicok.bac.yolo3d.scanner.Yolo3dNetwork;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
//        System.out.printf("Using TensorFlow %s\n", TensorFlow.version());

        final var path = "src/something/model.vox";
        final var inputFile = InputFileProvider.get(path);
        final var network = new Yolo3dNetwork("C:/src/bac/yolo-3d-poc/saved_model");
        try {
            network.loadWeights();
        } catch (UnsupportedKerasConfigurationException | InvalidKerasConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
        final var scanner = new Scanner(network);
        final var result = scanner.scan(inputFile);
        final var filter = new BoxFilter(0.5);
        final var boxes = filter.filter(result.objects());

        System.out.printf("Found %d objects.\n", boxes.size());
    }
}