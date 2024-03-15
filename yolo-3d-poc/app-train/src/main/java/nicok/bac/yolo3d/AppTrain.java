package nicok.bac.yolo3d;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.dataset.Category;
import nicok.bac.yolo3d.dataset.Label;
import nicok.bac.yolo3d.dataset.Model;
import nicok.bac.yolo3d.dataset.PsbDataset;
import nicok.bac.yolo3d.inputfile.InputFile;
import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.terminal.ProgressBar;
import nicok.bac.yolo3d.vox.VoxFileUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Runtime.getRuntime;
import static nicok.bac.yolo3d.preprocessing.RandomTransformation.randomTransformation;
import static nicok.bac.yolo3d.util.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.util.DirectoryUtil.getRepositoryRoot;
import static nicok.bac.yolo3d.util.DirectoryUtil.requireExtension;
import static nicok.bac.yolo3d.util.RepositoryPaths.DATASET_PSB;
import static nicok.bac.yolo3d.util.RepositoryPaths.DATASET_VOX;

public final class AppTrain {

    public static final Options OPTIONS = new Options()
            .addOption("e", "epochs", true, "Number of training epochs")
            .addOption("se", "super-epochs", true, "Number of training set generations")
            .addOption("pd", "prepare-dataset", true, "Regenerate dataset .vox files");
    public static final int INPUT_SIZE = 112;
    public static final BoundingBox TARGET_BOUNDING_BOX = BoundingBox.fromOrigin(INPUT_SIZE);

    public static void main(final String[] args) throws Exception {
        final var rootPath = getRepositoryRoot();

        // cli parsing
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var epochs = getEpochs(commandLine);
        final var superEpochs = getSuperEpochs(commandLine);
        final var prepareDataset = getPrepareDataset(commandLine);

        for (int currentSuperEpoch = 1; currentSuperEpoch <= superEpochs; currentSuperEpoch++) {
            System.out.printf("Super epoch %d / %d\n", currentSuperEpoch, superEpochs);

            // prepare dataset
            if (prepareDataset) {
                System.out.println("Preparing Dataset");
                prepareDataset();
            }

            // train
            System.out.println("Starting training. " + epochs + " epochs.");
            trainInPython(epochs, rootPath);
            System.out.print("\n");
        }
    }

    private static void trainInPython(int epochs, String rootPath) throws IOException {

        final var pythonDirectory = rootPath + "/yolo-3d-python";
        final var command = new String[]{"cmd.exe", "/c", "py -3.11 -m train", "--epochs", String.valueOf(epochs)};
        final var process = getRuntime().exec(command, null, new File(pythonDirectory));
        final var outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final var errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String line;
        while ((line = outputReader.readLine()) != null) {
            System.out.print("\r" + line.trim());
        }

        while ((line = errorReader.readLine()) != null) {
//            System.out.println("[PYTHON][ERROR]: " + line);
        }
    }

    private static void prepareDataset() throws Exception {
        final var dataset = new PsbDataset()
                .withPath(DATASET_PSB)
                .build();

        var currentProgress = 0;
        final var progressBar = new ProgressBar(20, dataset.trainModels().size());

        for (final var model : dataset.trainModels()) {
            // load 3d-model file
            final var modelFile = InputFileProvider.get(model.path());
            final var inputFile = randomTransformation(modelFile, TARGET_BOUNDING_BOX);

            checkInputFileBounds(inputFile);

            // voxelize & get label
            final var volume = inputFile.read(TARGET_BOUNDING_BOX);
            final var label = dataset.trainLabels().stream()
                    .filter(l -> l.modelId() == model.id())
                    .findFirst()
                    .orElseThrow();

            // save .vox and label files
            final var voxFileName = DATASET_VOX + "/" + model.id() + ".vox";
            final var txtFileName = DATASET_VOX + "/" + model.id() + ".txt";
            VoxFileUtil.saveVoxFile(voxFileName, volume);
            saveLabelFile(txtFileName, label, inputFile.getBoundingBox());

            ++currentProgress;
            progressBar.printProgress(currentProgress);
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

        saveSetFile(DATASET_VOX + "/train.txt", trainIds);
        saveSetFile(DATASET_VOX + "/val.txt", valIds);
        saveCategoriesFile(DATASET_VOX + "/categories.txt", dataset.categories());
    }

    private static void checkInputFileBounds(final InputFile inputFile) {
        if (inputFile.getBoundingBox().min().x() < TARGET_BOUNDING_BOX.min().x() ||
                inputFile.getBoundingBox().min().y() < TARGET_BOUNDING_BOX.min().y() ||
                inputFile.getBoundingBox().min().z() < TARGET_BOUNDING_BOX.min().z() ||
                inputFile.getBoundingBox().max().x() > TARGET_BOUNDING_BOX.max().x() ||
                inputFile.getBoundingBox().max().x() > TARGET_BOUNDING_BOX.max().y() ||
                inputFile.getBoundingBox().max().x() > TARGET_BOUNDING_BOX.max().z()
        ) {
            throw new IllegalStateException("FUCK");
        }
    }

    private static void saveCategoriesFile(
            final String path,
            final List<Category> categories
    ) throws IOException {
        requireExtension(path, ".txt");

        final var content = categories.stream()
                .map(category -> String.format("%d %s", category.id(), category.name()))
                .collect(Collectors.joining("\n"));

        try (final var writer = new FileWriter(path)) {
            writer.write(content);
        }
    }

    private static void saveSetFile(
            final String filename,
            final List<Integer> ids
    ) throws IOException {
        requireExtension(filename, ".txt");

        final var content = ids.stream()
                .map(id -> String.format("%s.vox %s.txt", id, id))
                .collect(Collectors.joining("\n"));

        try (final var writer = new FileWriter(filename)) {
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

        try (final var labelWriter = new FileWriter(filename)) {
            labelWriter.write(labelLine);
        }
    }

    private static int getSuperEpochs(final CommandLine commandLine) {
        final var epochString = commandLine.getOptionValue("super-epochs", "1");
        try {
            return Integer.parseInt(epochString);
        } catch (final NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
            return 1;
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