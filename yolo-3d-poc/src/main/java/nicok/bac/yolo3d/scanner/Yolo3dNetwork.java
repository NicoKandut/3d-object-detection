package nicok.bac.yolo3d.scanner;

import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.tensorflow.SavedModelBundle;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public record Yolo3dNetwork(String file) implements Network {

    public void loadWeights() throws IOException, UnsupportedKerasConfigurationException, InvalidKerasConfigurationException {
//        final var network = KerasModelImport.importKerasModelAndWeights(file);
        SavedModelBundle.load(file);
    }


    @Override
    public Point getExtent() {
        return new Point(28, 28, 28);
    }

    @Override
    public List<ResultBoundingBox> compute(Volume3D volume) {
        return Collections.emptyList();
    }
}
