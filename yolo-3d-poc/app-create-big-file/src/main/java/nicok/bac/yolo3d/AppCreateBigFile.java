package nicok.bac.yolo3d;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.dataset.PsbDataset;
import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.preprocessing.FitToBox;
import nicok.bac.yolo3d.storage.bff.BffWriterRAF;
import nicok.bac.yolo3d.storage.off.OffReader;
import nicok.bac.yolo3d.terminal.ProgressBar;
import nicok.bac.yolo3d.util.RepositoryPaths;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;
import static nicok.bac.yolo3d.preprocessing.RandomTransformation.getRandomRotation;
import static nicok.bac.yolo3d.preprocessing.RandomTransformation.getRandomShift;
import static nicok.bac.yolo3d.terminal.CommandLineUtil.*;

public class AppCreateBigFile {

    public static final Options OPTIONS = new Options()
            .addOption("o", "output", true, "Output path")
            .addOption("n", "n", true, "Number of models to put into the big file")
            .addOption("ms", "model-size", true, "Size of the models in format min,max")
            .addOption("s", "size", true, "Size of the file in format x,y,z")
            .addOption("h", "help", false, "Display this help message");

    public static void main(final String[] args) throws Exception {

        // parse CLI arguments
        final var commandLine = parseCommandLine("AppCreateBigFile", args, OPTIONS);
        final var outputPath = parseFilePath(commandLine, "output", ".bff", ".off");
        final var size = parsePoint(commandLine, "size", "size argument must not be empty");
        final var modelSize = parseDoubleRange(commandLine, "model-size", "model-size must not be empty");
        final var minModelSize = modelSize.getFirst();
        final var maxModelSize = modelSize.getSecond();
        final var n = parseInt(commandLine.getOptionValue("n"));

        // init dataset
        final var dataset = new PsbDataset()
                .withPath(RepositoryPaths.DATASET_PSB)
                .withSelectedCategories(List.of("biplane"))
                .build();

        final var random = new Random();

        System.out.println("Loading models");

        // choose n models
        final var models = IntStream.range(0, n)
                .map(i -> random.nextInt(dataset.trainModels().size()))
                .mapToObj(modelIndex -> dataset.trainModels().get(modelIndex))
                .toList();

        // read their headers
        final var modelHeaders = models.stream()
                .map(model -> {
                    try (final var modelReader = new OffReader(model.path())) {
                        return modelReader.readHeader().vertexCount();
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        final var totalVertexCount = modelHeaders.stream().mapToLong(Long::longValue).sum();

        // create big file
        System.out.println("Placing models into big file");
        final var progressBar = new ProgressBar(20, n);
        final var resultBoundingBox = BoundingBox.fromOrigin(size);
        try (final var resultWriter = new BffWriterRAF(outputPath, 4, 8)) {

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
                        // adjust vertex indices for the new file
                        final var adjustedFace = new Face(
                                face.vertexIndices().stream()
                                        .map(index -> currentVertexIdOffset + index)
                                        .toList()
                        );
                        resultWriter.writeFace(adjustedFace, totalVertexCount);
                    }
                }

                vertexIdOffset += modelHeaders.get(i);
                progressBar.printProgress(i + 1);
            }

            resultWriter.writeHeader();
        }

        System.out.printf("Done. File saved at: %s\n", new File(outputPath).getAbsolutePath());
    }
}
