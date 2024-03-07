package nicok.bac.yolo3d;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.dataset.Label;
import nicok.bac.yolo3d.dataset.Model;
import nicok.bac.yolo3d.dataset.PsbDataset;
import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.preprocessing.FitToBox;
import nicok.bac.yolo3d.vox.VoxFileUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Runtime.getRuntime;
import static nicok.bac.yolo3d.util.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.util.DirectoryUtil.getRepositoryRoot;

public final class Main {

    private static final String VOX_DATASET_PATH = "/dataset-psb-vox";

    public static final Options OPTIONS = new Options()
            .addOption("e", "epochs", true, "Number of training epochs")
            .addOption("pd", "prepare-dataset", true, "Regenerate dataset .vox files");
    public static final int INPUT_SIZE = 224;

    public static void main(final String[] args) throws Exception {
        final var rootPath = getRepositoryRoot();

        // cli parsing
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var epochs = getEpochs(commandLine);
        final var prepareDataset = getPrepareDataset(commandLine);

        // prepare dataset
        if (prepareDataset) {
            System.out.println("Preparing Dataset");
            prepareDataset(rootPath);
        }

        // train
        System.out.println("Starting training. " + epochs + " epochs.");
        final var pythonDirectory = rootPath + "/yolo-3d-python";
        final var command = new String[]{"cmd.exe", "/c", "py -3.11 -m train", "--epochs", String.valueOf(epochs)};
        final var process = getRuntime().exec(command, null, new File(pythonDirectory));
        final var outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final var errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        outputReader.lines().forEach(System.out::println);
        errorReader.lines().forEach(System.out::println);
    }

    private static void prepareDataset(final String rootPath) throws Exception {
        final var dataset = new PsbDataset()
                .withPath(rootPath + "/dataset-psb")
                .build();

        // scale 3d-models to cnn-input
        final var preprocessing = new FitToBox()
                .withTargetBoundingBox(new BoundingBox(
                        Point.ZERO,
                        new Point(INPUT_SIZE, INPUT_SIZE, INPUT_SIZE)
                ));

        final var voxDatasetPath = rootPath + VOX_DATASET_PATH + "/";

        for (final var model : dataset.trainModels()) {
            // load 3d-model file
            final var inputFile = InputFileProvider.get(model.path());

            // apply transformation
            preprocessing.withSourceBoundingBox(inputFile.getBoundingBox());
            inputFile.withPreprocessing(preprocessing);

            // voxelize & get label
            final var volume = inputFile.read(inputFile.getBoundingBox());
            final var label = dataset.trainLabels().stream()
                    .filter(l -> l.modelId() == model.id())
                    .findFirst()
                    .orElseThrow();

            // save .vox and label files
            VoxFileUtil.saveVoxFile(voxDatasetPath + model.id() + ".vox", volume);
            saveLabelFile(voxDatasetPath, label, inputFile.getBoundingBox());

            System.out.printf(" [m%4d] size: %s\n", model.id(), inputFile.getBoundingBox().size());
        }

        final var ids = dataset.trainModels().stream()
                .map(Model::id)
                .collect(Collectors.toList());
        Collections.shuffle(ids);
        final var splitIndex = (int) (ids.size() * 0.9);
        final var trainIds = ids.subList(0, splitIndex);
        final var valIds = ids.subList(splitIndex, ids.size());

        trainIds.sort(Integer::compare);
        valIds.sort(Integer::compare);

        saveSetFile(voxDatasetPath + "train", trainIds);
        saveSetFile(voxDatasetPath + "val", valIds);
    }

    private static void saveSetFile(
            final String filename,
            final List<Integer> ids
    ) throws IOException {
        final var content = ids.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));

        try (final var writer = new FileWriter(filename + ".txt")) {
            writer.write(content);
        }
    }

    private static void saveLabelFile(
            final String filename,
            final Label label,
            final BoundingBox boundingBox
    ) throws IOException {
        final var min = boundingBox.min();
        final var max = boundingBox.max();
        final var labelLine = String.format(
                "%d %s %s %s %s %s %s\n",
                label.categoryId(),
                min.x(),
                min.y(),
                min.z(),
                max.x(),
                max.y(),
                max.z()
        );

        try (final var labelWriter = new FileWriter(filename + ".txt")) {
            labelWriter.write(labelLine);
        }
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

    private static boolean getPrepareDataset(final CommandLine commandLine) {
        final var stringValue = commandLine.getOptionValue("prepare-dataset", "true");
        return Boolean.parseBoolean(stringValue);
    }
}