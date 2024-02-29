package nicok.bac.yolo3d.dataset;

import org.tensorflow.Tensor;

import java.util.Collections;
import java.util.List;

public class PrincetonShapeBenchmark {

    public static record Batch(
            List<Tensor> x,
            List<Tensor> y
    ) {
    }

    public List<Batch> getTrainingBatches(int batchSize) {
        return Collections.emptyList();
    }
}
