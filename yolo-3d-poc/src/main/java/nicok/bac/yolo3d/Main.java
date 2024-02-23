package nicok.bac.yolo3d;

import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.scanner.Scanner;
import nicok.bac.yolo3d.network.Yolo3dNetwork;
import org.tensorflow.TensorFlow;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.printf("Using TensorFlow %s\n", TensorFlow.version());

        final var path = "C:/Users/nicok/IdeaProjects/ui/dataset-3d-minecraft-1k/train_5.vox";
        final var inputFile = InputFileProvider.get(path);
        final var network = new Yolo3dNetwork("C:/Users/nicok/IdeaProjects/ui/yolo-3d-poc/saved_model");
        network.loadWeights();
        final var scanner = new Scanner(network);
        final var result = scanner.scan(inputFile);
        final var filter = new BoxFilter(0.5);
        final var boxes = filter.filter(result.objects());

        System.out.printf("Found %d objects.\n", boxes.size());
    }
}