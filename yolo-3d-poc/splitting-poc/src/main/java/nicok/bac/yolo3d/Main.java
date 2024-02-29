package nicok.bac.yolo3d;

import com.scs.voxlib.VoxFile;
import com.scs.voxlib.VoxWriter;
import com.scs.voxlib.chunk.VoxRootChunk;
import com.scs.voxlib.chunk.VoxSizeChunk;
import com.scs.voxlib.chunk.VoxXYZIChunk;
import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.inputfile.InputFile;
import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.off.Vertex;
import nicok.bac.yolo3d.preprocessing.PreProcessing;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

public class Main {

    public static final String REPOSITORY_PATH = "C:/Users/nicok/IdeaProjects/bac";
    public static final String SAVED_MODEL_PATH = REPOSITORY_PATH + "/yolo-3d-poc/saved_model";
    public static final String VOX_PATH = REPOSITORY_PATH + "/dataset-3d-minecraft/test_0.vox";
    public static final String OFF_PATH = REPOSITORY_PATH + "/mushroom.off";

    public static void main(String[] args) throws Exception {
        final var preprocessing = new PreProcessing.Builder()
                .rotate(0.1, 0.2,0.5)
                .shift(new Vertex(10, 5, -2))
                .scaling(6.0)
                .build();
        final var inputFile = InputFileProvider.get(OFF_PATH, preprocessing);

        // save chunks
        final var boxes = getBoundingBoxes(inputFile);
        final var ids = IntStream.range(0, 1000).iterator();
        for (final var box : boxes) {
            final var volume = inputFile.read(box);
            final var filename = "mushroom.vox";
            saveVoxFile(filename, volume);
        }
    }

//    public static void main(String[] args) throws IOException {
//        System.out.printf("Using TensorFlow %s\n", TensorFlow.version());
//
//        final var inputFile = InputFileProvider.get(VOX_PATH);
//        final var network = new Yolo3dNetwork(SAVED_MODEL_PATH);
//        final var scanner = new Scanner(network);
//        final var result = scanner.scan(inputFile);
//        final var filter = new BoxFilter(0.6, 0.5);
//        final var boxes = filter.filter(result.objects());
//
//        System.out.printf("Found %d objects.\n", boxes.size());
//
//        for(final var box : boxes) {
//            System.out.printf("  - %s\n", box);
//        }
//    }


    private static List<BoundingBox> getBoundingBoxes(InputFile inputFile) {
        final var min = inputFile.getBoundingBox().min();
        final var mid = inputFile.getBoundingBox().center();
        final var max = inputFile.getBoundingBox().max();

         return List.of(inputFile.getBoundingBox());
//        return List.of(
//                new BoundingBox(
//                        min,
//                        mid
//                ),
//                new BoundingBox(
//                        new Point(min.x(), min.y(), mid.z()),
//                        new Point(mid.x(), mid.y(), max.z())
//                ),
//                new BoundingBox(
//                        new Point(min.x(), mid.y(), min.z()),
//                        new Point(mid.x(), max.y(), mid.z())
//                ),
//                new BoundingBox(
//                        new Point(mid.x(), min.y(), min.z()),
//                        new Point(max.x(), mid.y(), mid.z())
//                ),
//                new BoundingBox(
//                        new Point(mid.x(), mid.y(), min.z()),
//                        new Point(max.x(), max.y(), mid.z())
//                ),
//                new BoundingBox(
//                        new Point(mid.x(), min.y(), mid.z()),
//                        new Point(max.x(), mid.y(), max.z())
//                ),
//                new BoundingBox(
//                        new Point(min.x(), mid.y(), mid.z()),
//                        new Point(mid.x(), max.y(), max.z())
//                ),
//                new BoundingBox(
//                        mid,
//                        max
//                )
//        );
    }

    private static void saveVoxFile(String filename, Volume3D volume) {
        try (final var writer = new VoxWriter(new FileOutputStream(filename))) {
            final var root = new VoxRootChunk();

            final var size = volume.boundingBox().size();
            final var sizeChunk = new VoxSizeChunk((int) Math.ceil(size.x()), (int) Math.ceil(size.y()), (int) Math.ceil(size.z()));
            root.appendChunk(sizeChunk);

            final var voxels = volume.toVoxels();
            final var chunk = new VoxXYZIChunk(voxels);
            root.appendChunk(chunk);

            System.out.println("Saving to " + filename);

            writer.write(new VoxFile(VoxWriter.VERSION, root));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}