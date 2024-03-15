package nicok.bac.yolo3d;

import nicok.bac.yolo3d.bff.BffWriterRAF;
import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.dataset.PsbDataset;
import nicok.bac.yolo3d.off.Face;
import nicok.bac.yolo3d.off.OffReader;
import nicok.bac.yolo3d.preprocessing.FitToBox;
import nicok.bac.yolo3d.util.RepositoryPaths;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;
import static nicok.bac.yolo3d.preprocessing.RandomTransformation.getRandomRotation;
import static nicok.bac.yolo3d.preprocessing.RandomTransformation.getRandomShift;
import static nicok.bac.yolo3d.util.CommandLineUtil.*;
import static nicok.bac.yolo3d.util.DirectoryUtil.requireExtension;

public class AppScanBigFile {

    public static final Options OPTIONS = new Options()
            .addRequiredOption("o", "output", true, "Output path")
            .addRequiredOption("n", "n", true, "Number of models to put into the big file")
            .addRequiredOption("ms", "model-size", true, "Size of the models in format min,max")
            .addRequiredOption("s", "size", true, "Size of the file in format x,y,z");

    /**
     * Usage: :app-create-big-file:run
     */
    public static void main(final String[] args) throws Exception {

        // parse CLI arguments
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var outputPath = getOutputPath(commandLine);
        final var size = parsePoint(commandLine, "size");
        final var modelSize = parseDoubleRange(commandLine, "model-size");
        final var minModelSize = modelSize.getFirst();
        final var maxModelSize = modelSize.getSecond();
        final var n = parseInt(commandLine.getOptionValue("n"));

        // init dataset
        final var dataset = new PsbDataset()
                .withPath(RepositoryPaths.DATASET_PSB)
                .build();

        final var random = new Random();

        // choose n models
        final var models = IntStream.range(0, n)
                .map(i -> random.nextInt(dataset.trainModels().size()))
                .mapToObj(modelIndex -> dataset.trainModels().get(modelIndex))
                .toList();

        final var modelHeaders = models.stream()
                .map(model -> {
                    try (final var modelReader = new OffReader(model.path())) {
                        return modelReader.readHeader().vertexCount();
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        final var totalVertexCount = modelHeaders.stream().mapToInt(Integer::intValue).sum();

        // create big file
        final var resultBoundingBox = BoundingBox.fromOrigin(size);
        try (final var resultWriter = new BffWriterRAF(outputPath)) {

            var vertexIdOffset = 0;

            // transform and write each model
            for (int i = 0; i < n; i++) {
                final var model = models.get(i);

                // read model
                try (final var modelReader = new OffReader(model.path())) {
                    var mesh = modelReader.readMesh();

                    // apply random rotation
                    final var rotation = getRandomRotation(mesh.boundingBox(), random);
                    mesh = mesh.applyTransformation(rotation);

                    // apply random scaling (and align to origin)
                    final var newSize = minModelSize + random.nextDouble() * (maxModelSize - minModelSize);
                    final var scaling = new FitToBox()
                            .withSourceBoundingBox(mesh.boundingBox())
                            .withTargetBoundingBox(BoundingBox.fromOrigin(newSize));
                    mesh = mesh.applyTransformation(scaling);

                    // apply random offset
                    final var shift = getRandomShift(resultBoundingBox, mesh.boundingBox(), random);
                    mesh = mesh.applyTransformation(shift);

                    // write to big file
                    for (final var vertex : mesh.vertices()) {
                        resultWriter.writeVertex(vertex);
                    }
                    final var currentVertexIdOffset = vertexIdOffset;
                    for (final var face : mesh.faces()) {
                        final var adjustedFace = new Face(
                                face.vertexIndices().stream()
                                        .map(index -> currentVertexIdOffset + index)
                                        .toList()
                        );

                        resultWriter.writeFace(adjustedFace, totalVertexCount);
                    }
                }

                vertexIdOffset += modelHeaders.get(i);
            }

            resultWriter.writeHeader();
        }
    }

    private static String getOutputPath(CommandLine commandLine) {
        final var outputPath = commandLine.getOptionValue("output");
        requireExtension(outputPath, ".off", ".bff");
        return outputPath;
    }
}
