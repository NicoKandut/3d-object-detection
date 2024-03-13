package nicok.bac.yolo3d;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.inputfile.InputFileProvider;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.preprocessing.RandomTransformation.randomTransformation;
import static nicok.bac.yolo3d.util.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.util.DirectoryUtil.getFilename;
import static nicok.bac.yolo3d.vox.VoxFileUtil.saveVoxFile;

public class AppVoxelize {
    public static final Options OPTIONS = new Options()
            .addRequiredOption("i", "input", true, "Input file.")
            .addRequiredOption("o", "output", true, "Output file.")
            .addOption("s", "split", true, "Split the model into equal boxes along each axis");
    private static final BoundingBox TARGET_BOUNDING_BOX = new BoundingBox(
            Point.ZERO,
            new Point(112, 112, 112)
    );

    /**
     * Usage: :app-voxelize:run --args='-i path/to/file.off -o path/to/output/dir'
     */
    public static void main(String[] args) throws Exception {
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var inputPath = commandLine.getOptionValue("input");
        final var outputPath = commandLine.getOptionValue("output");
        final var splitSize = getSplitSize(commandLine);

        requireNonNull(inputPath);
        requireNonNull(outputPath);

        // load 3d-model file
        final var baseFile = InputFileProvider.get(inputPath);
        final var inputFile = randomTransformation(baseFile, TARGET_BOUNDING_BOX);

        final var inputFileName = getFilename(inputPath);
        System.out.printf("File bounds: %s\n", inputFile.getBoundingBox());

        System.out.println("Saving VOX file");
        final var filename = outputPath + "/" + inputFileName + ".vox";

        final var volume = inputFile.read(TARGET_BOUNDING_BOX);
        System.out.printf("  Saving %s to %s\n", TARGET_BOUNDING_BOX, filename);
        saveVoxFile(filename, volume);

        System.out.println("DONE");
    }

    private static int getSplitSize(final CommandLine commandLine) {
        try {
            return Integer.parseInt(commandLine.getOptionValue("split"));
        } catch (final NumberFormatException ignored) {
            return 1;
        }
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