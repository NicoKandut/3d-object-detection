package nicok.bac.yolo3d;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.inputfile.InputFile;
import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.preprocessing.LinearTransformation;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.util.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.util.DirectoryUtil.getFilename;
import static nicok.bac.yolo3d.vox.VoxFileUtil.saveVoxFile;

public class AppVoxelize {
    public static final Options OPTIONS = new Options()
            .addRequiredOption("i", "input", true, "Input file.")
            .addRequiredOption("o", "output", true, "Output file.")
            .addOption("s", "split", true, "Split the model into equal boxes along each axis");

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

        final var preprocessing = new LinearTransformation.Builder()
//                .rotate(0.1, 0.2, 0.5)
                .scaling(12.0)
                .build();
        var inputFile = InputFileProvider.get(inputPath);
        inputFile = inputFile.withPreprocessing(preprocessing);

        final var inputFileName = getFilename(inputPath);
        System.out.printf("File bounds: %s\n", inputFile.getBoundingBox().size());

        final var boxes = getBoundingBoxes(inputFile, splitSize);
        System.out.printf("Splitting will generate %d boxes\n", boxes.size());

        System.out.println("Saving VOX files:");
        for (int i = 0; i < boxes.size(); i++) {
            final var box = boxes.get(i);

            final var filename = boxes.size() > 1
                    ? outputPath + "/" + inputFileName + "_" + i + ".vox"
                    : outputPath + "/" + inputFileName + ".vox";

            final var volume = inputFile.read(box);
            System.out.printf("  Saving to %s\n", filename);
            saveVoxFile(filename, volume);
        }

        System.out.println("DONE");
    }

    private static int getSplitSize(CommandLine commandLine) {
        try {
            return Integer.parseInt(commandLine.getOptionValue("split"));
        } catch (final NumberFormatException ignored) {
            return 1;
        }
    }

    private static List<BoundingBox> getBoundingBoxes(
            final InputFile inputFile,
            final int splits
    ) {
        final var min = inputFile.getBoundingBox().min();
        final var step = Point.mul(1.0 / (double) splits, inputFile.getBoundingBox().size());
        final var boxes = new ArrayList<BoundingBox>();

        for (int z = 0; z < splits; z++) {
            for (int y = 0; y < splits; y++) {
                for (int x = 0; x < splits; x++) {
                    final var offset = new Point(
                            x * step.x(),
                            y * step.y(),
                            z * step.z()
                    );
                    final var from = Point.add(min, offset);
                    final var to = Point.add(from, step);
                    final var box = new BoundingBox(from, to);

                    boxes.add(box);
                }
            }
        }

        return boxes;
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