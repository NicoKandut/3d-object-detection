package nicok.bac.yolo3d.model;

import org.tensorflow.Graph;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.ndarray.Shape;

import java.io.IOException;
import java.util.List;


public class Yolo3D {

    public static final String INPUT_NAME = "input";
    public static final String OUTPUT_NAME = "output";
    public static final long INPUT_SIZE = 28;
    public static final int INPUT_CHANNELS = 1;
    public static final Shape INPUT_SHAPE = Shape.of(INPUT_SIZE, INPUT_SIZE, INPUT_SIZE, INPUT_CHANNELS);
    public static final int CONVOLUTION_SIZE = 3;
    public static final List<Long> CONV_STRIDE = List.of(1L, 1L, 1L);

    private static final long SEED = 123456789L;
    public static final List<Long> POOL_SIZE_2 = List.of(2L, 2L, 2L);
    public static final List<Long> STRIDE_1 = List.of(1L, 1L, 1L);
    public static final String SAME = "same";

    public static final String SAVED_MODEL_PATH = "C:\\src\\bac\\yolo-3d-poc\\py\\saved_model";
    private final SavedModelBundle savedModel;

    public Yolo3D() {
        this.savedModel = SavedModelBundle.load(SAVED_MODEL_PATH);

        final var optimizer = new Adam(graph, 0.001f, 0.9f, 0.999f, 1e-8f);

        optimizer.minimize(loss, TRAIN);
    }

    public Graph graph() {
        return this.savedModel.graph();
    }

    public Session session() {
        return this.savedModel.session();
    }

    public void save() throws IOException {
        SavedModelBundle.exporter(SAVED_MODEL_PATH)
                .withSession(this.savedModel.session())
                .export();
    }


//    public static Graph create() {
//        final var graph = new Graph();
//        final var tf = Ops.create(graph);
//
//        final var input = tf
//                .withName(INPUT_NAME)
//                .placeholder(TFloat32.class, Placeholder.shape(INPUT_SHAPE));
//
//        final var convolution1 = convolutionLayer("1", tf, input, 512);
//        final var pooling1 = maxPoolingLayer(tf, convolution1);
//
//        final var convolution2 = convolutionLayer("1", tf, pooling1, 1024);
//        final var convolution3 = convolutionLayer("1", tf, convolution2, 256);
//
//        tf.withName(OUTPUT_NAME).shape.flatten(convolution3);
//
//        return graph;
//    }
//
//    public static Operand<TFloat32> maxPoolingLayer(Ops tf, Operand<TFloat32> input) {
//        return tf.nn.maxPool3d(input, POOL_SIZE_2, STRIDE_1, SAME);
//    }
//
//    public static Operand<TFloat32> convolutionLayer(
//            String layerName,
//            Ops tf,
//            Operand<TFloat32> input,
//            int filters
//    ) {
//        final var convWeights = tf
//                .withName("conv2d_" + layerName)
//                .variable(
//                        tf.math.mul(
//                                tf.random.truncatedNormal(
//                                        tf.array(CONVOLUTION_SIZE, CONVOLUTION_SIZE, CONVOLUTION_SIZE, INPUT_CHANNELS, filters),
//                                        TFloat32.class,
//                                        TruncatedNormal.seed(SEED)
//                                ),
//                                tf.constant(0.1f)
//                        )
//                );
//
//        final var conv = tf.nn.conv3d(input, convWeights, STRIDE_1, SAME);
//
//        final var mean = tf.constant(0f);
//        final var variance = tf.constant(1f);
//        final var offset = tf.constant(0f);
//        final var scale = tf.constant(1f);
//
//        final var norm = tf.nn.fusedBatchNorm(conv, scale, offset, mean, variance).y();
//
//        return tf.nn.leakyRelu(norm, alpha(0.1f));
//    }
}
