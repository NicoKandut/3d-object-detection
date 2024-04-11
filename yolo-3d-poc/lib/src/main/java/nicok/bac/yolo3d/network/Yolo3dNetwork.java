package nicok.bac.yolo3d.network;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.common.CellOutput;
import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.mesh.Vertex;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.StdArrays;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Yolo3dNetwork implements Network, AutoCloseable {

    public static final long SIZE = 112;

    private final SavedModelBundle savedModelBundle;

    public Yolo3dNetwork(final String file) {
        savedModelBundle = SavedModelBundle.load(file);
    }

    @Override
    public Vertex size() {
        return new Vertex(SIZE, SIZE, SIZE);
    }

    @Override
    public List<ResultBoundingBox> compute(final BoundingBox frame, final Volume3D volume) {
        if (!frame.size().equals(volume.getBoundingBox().size())) {
            throw new IllegalArgumentException("Frame and volume size must be equal");
        }

        if (!frame.size().equals(this.size())) {
            throw new IllegalArgumentException("Frame and volume size must be equal to network size");
        }

        final var input = Map.of("input_1", volume.toTensor());
        final var boundingBoxes = new ArrayList<ResultBoundingBox>();

        try (final var result = savedModelBundle.call(input)) {
            final var outputData = StdArrays.array5dCopyOf((FloatNdArray) result.get(0))[0];
            for (var i = 0; i < result.get(0).shape().get(1); i++) {
                for (var j = 0; j < result.get(0).shape().get(2); j++) {
                    for (var k = 0; k < result.get(0).shape().get(3); k++) {
                        final var prediction = outputData[i][j][k];
                        final var cellOutput = CellOutput.fromOutputArray(prediction);
                        final var boxReal = ResultBoundingBox.fromOutput(this, cellOutput, frame, i, j, k);
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
