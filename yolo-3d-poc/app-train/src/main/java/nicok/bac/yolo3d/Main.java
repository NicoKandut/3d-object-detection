package nicok.bac.yolo3d;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.dataset.PsbDataset;
import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.preprocessing.FitToBox;
import nicok.bac.yolo3d.vox.VoxFileUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.FileWriter;

import static nicok.bac.yolo3d.util.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.util.DirectoryUtil.getRepositoryRoot;

public final class Main {

    private static final String VOX_DATASET_PATH = "/dataset-psb-vox";

    public static final Options OPTIONS = new Options()
            .addOption("e", "epochs", true, "Number of training epochs");
    public static final int INPUT_SIZE = 224;

    public static void main(final String[] args) throws Exception {
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var epochs = getEpochs(commandLine);
        final var rootPath = getRepositoryRoot();
        final var dataset = new PsbDataset()
                .withPath(rootPath + "/dataset-psb")
                .build();

        final var preprocessing = new FitToBox()
                .withTargetBoundingBox(new BoundingBox(
                        Point.ZERO,
                        new Point(INPUT_SIZE, INPUT_SIZE, INPUT_SIZE)
                ));

        for (final var model : dataset.trainModels()) {
            final var inputFile = InputFileProvider.get(model.path());
            preprocessing.withSourceBoundingBox(inputFile.getBoundingBox());
            inputFile.withPreprocessing(preprocessing);
            System.out.printf(" [m%4d] size: %s\n", model.id(), inputFile.getBoundingBox().size());
            final var volume = inputFile.read(inputFile.getBoundingBox());
            final var filename = rootPath + VOX_DATASET_PATH + "/" + model.id();
            VoxFileUtil.saveVoxFile(filename + ".vox", volume);

            final var label = dataset.trainLabels().stream()
                    .filter(l -> l.modelId() == model.id())
                    .findFirst()
                    .orElseThrow();

            final var labelWriter = new FileWriter(filename + ".txt");
            final var min = inputFile.getBoundingBox().min();
            final var max = inputFile.getBoundingBox().max();
            labelWriter.write(label.categoryId() + " "
                    + min.x() + " " + min.y() + " " + min.z() + " "
                    + max.x() + " " + max.y() + " " + max.z() + "\n"
            );
            labelWriter.close();
        }

//        final var model = new Yolo3D();
//
//        for (var i = 0; i < epochs; ++i) {
//
//        }
    }

    private static int getEpochs(final CommandLine commandLine) {
        final var epochString = commandLine.getOptionValue("epochs", "1");
        try {
            return Integer.parseInt(epochString);
        } catch (final NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
            return 1;
        }
    }
}