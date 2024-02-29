package nicok.bac.yolo3d;

//import org.tensorflow.Graph;
//import org.tensorflow.Session;

public class Yolo3d {
//    public static Session createModel(int voxelSize, int numClasses, int numBoxes) {
////        final var network = KerasModelImport.importKerasSequentialModelAndWeights("PATH TO YOUR JSON FILE", "PATH TO YOUR H5 FILE")
////
////
//        try (final var graph = new Graph()) {
//
//
//            // Input placeholder for voxel data
//            final var inputPlaceholder = tf.placeholder(TFloat32.class, Placeholder.shape(Shape.of(voxelSize, voxelSize, voxelSize)));
//
//            // 3D Convolutional Layers for feature extraction
//            final var convWeights = tf.variable(tf.random.randomStandardNormal(tf.constant(new int[]{3, 3, 3, 1, 64}), TFloat32.class));
//            final var convBias = tf.variable(tf.zeros(tf.constant(64), TFloat32.class));
//            final var l1 = tf.nn.leakyRelu(
//                    tf.math.add(
//                            tf.nn.conv3d(
//                                    inputPlaceholder,
//                                    convWeights,
//                                    tf.constant(new int[]{1, 1, 1, 1, 1}),
//                                    "SAME"
//                            ),
//                            convBias
//                    )
//            );
//
//            // Add more 3D convolutional layers as needed
//
//            // Flatten the 3D feature maps
//            final var flattenedSize = voxelSize * voxelSize * voxelSize;
//            final var l2 = tf.reshape(l1, tf.constant(-1, flattenedSize));
//
//            // Fully connected layers for detection
//            final var denseWeights1 = tf.variable(tf.randomNormal(tf.constant(flattenedSize, 4096), Float.class));
//            final var denseBias1 = tf.variable(tf.zeros(tf.constant(4096), Float.class));
//            final var l3 = tf.nn.relu(tf.add(tf.linalg.matMul(l2, denseWeights1), denseBias1));
//
//            final var denseWeights2 = tf.variable(tf.randomNormal(tf.constant(4096, 4096), Float.class));
//            final var denseBias2 = tf.variable(tf.zeros(tf.constant(4096), Float.class));
//            final var l4 = tf.nn.relu(tf.add(tf.linalg.matMul(l3, denseWeights2), denseBias2));
//
//            // Output layer for bounding box and class predictions
//            final var outputWeights = tf.variable(tf.randomNormal(tf.constant(4096, numBoxes * (4 + 1 + numClasses)), Float.class));
//            final var outputBias = tf.variable(tf.zeros(tf.constant(numBoxes * (4 + 1 + numClasses)), Float.class));
//            final var output = tf.add(tf.linalg.matMul(l4, outputWeights), outputBias);
//
//            graph.toGraphDef();
//            return new Session(graph);
//        }
//    }
}
