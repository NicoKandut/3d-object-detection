package nicok.bac.yolo3d.network;

import nicok.bac.yolo3d.common.*;
import nicok.bac.yolo3d.off.Vertex;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.StdArrays;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Yolo3dNetwork implements Network, AutoCloseable {

    private final SavedModelBundle savedModelBundle;

    public Yolo3dNetwork(final String file) {
        savedModelBundle = SavedModelBundle.load(file);
    }

    @Override
    public Vertex getExtent() {
        return new Vertex(28, 28, 28);
    }

    @Override
    public List<ResultBoundingBox> compute(BoundingBox frame, Volume3D volume) {
        final Map<String, Tensor> input = Map.of(
                "input_1",
                volume.toTensor()
        );

        final var boundingBoxes = new ArrayList<ResultBoundingBox>();

        try (final var result = savedModelBundle.call(input)) {
            final var outputData = StdArrays.array5dCopyOf((FloatNdArray) result.get(0))[0];
            for (var i = 0; i < result.get(0).shape().get(1); i++) {
                for (var j = 0; j < result.get(0).shape().get(2); j++) {
                    for (var k = 0; k < result.get(0).shape().get(3); k++) {
                        final var prediction = outputData[i][j][k];
                        final var cellOutput = CellOutput.fromOutputArray(prediction);
                        final var boxReal = ResultBoundingBox.fromOutput(cellOutput, frame, i, j, k);
                        boundingBoxes.add(boxReal);
                    }
                }
            }
        }

        return boundingBoxes;
    }

    @Override
    public void close() {
        savedModelBundle.close();
    }
}
