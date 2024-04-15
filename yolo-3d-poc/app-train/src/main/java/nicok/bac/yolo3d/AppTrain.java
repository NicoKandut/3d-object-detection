package nicok.bac.yolo3d;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.dataset.PsbDataset;
import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.network.Yolo3dNetwork;
import nicok.bac.yolo3d.terminal.ProgressBar;
import org.apache.commons.cli.Options;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Runtime.getRuntime;
import static nicok.bac.yolo3d.dataset.VoxDatasetUtils.*;
import static nicok.bac.yolo3d.preprocessing.RandomTransformation.randomTransformation;
import static nicok.bac.yolo3d.terminal.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.terminal.CommandLineUtil.parseOptionalInt;
import static nicok.bac.yolo3d.util.RepositoryPaths.*;
import static nicok.bac.yolo3d.vox.VoxFileUtil.saveVoxFile;

public final class AppTrain {

    public static final Options OPTIONS = new Options()
            .addOption("e", "epochs", true, "Number of training epochs")
            .addOption("se", "super-epochs", true, "Number of training set generations")
            .addOption("v", "variations", true, "Number of variations per model")
            .addOption("pd", "prepare-dataset", false, "Regenerate dataset .vox files")
            .addOption("h", "help", false, "Display this help message");

    public static final BoundingBox TARGET_BOUNDING_BOX = BoundingBox.fromOrigin(Yolo3dNetwork.SIZE);
    public static final String INVALID_TRANSFORMATION = "This should not happen. There might be a bug in the randomTransformation method.";

    public static void main(final String[] args) throws Exception {

        // cli parsing
        final var commandLine = parseCommandLine("AppTrain", args, OPTIONS);
        final var epochs = parseOptionalInt(commandLine, "epochs", 1);
        final var superEpochs = parseOptionalInt(commandLine, "super-epochs", 1);
        final var prepareDataset = commandLine.hasOption("prepare-dataset") || commandLine.hasOption("pd");
        final var variations = parseOptionalInt(commandLine, "variations", 1);

        for (int currentSuperEpoch = 1; currentSuperEpoch <= superEpochs; currentSuperEpoch++) {
            System.out.printf("Super epoch %d / %d\n", currentSuperEpoch, superEpochs);

            // prepare dataset
            if (prepareDataset) {
                System.out.println("Preparing Dataset");
                prepareDataset(variations);
            }

            // train
            System.out.println("Starting training. " + epochs + " epochs.");
            trainInPython(epochs);
            System.out.print("\n");
        }
    }

    private static void trainInPython(final int epochs) throws IOException {
        final var command = new String[]{"cmd.exe", "/c", "py -3.11 -m train", "--epochs", String.valueOf(epochs)};
        final var process = getRuntime().exec(command, null, new File(YOLO_3D_PYTHON));
        final var outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final var errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String line;
        while ((line = outputReader.readLine()) != null) {
            System.out.print("\r" + line.trim());
        }

        while ((line = errorReader.readLine()) != null) {
            System.out.println("[PYTHON][ERROR]: " + line);
        }
    }

    private static void prepareDataset(final long variations) throws Exception {
        final var dataset = new PsbDataset()
                .withPath(DATASET_PSB)
                .withSelectedCategories(List.of("biplane", "ship", "hammer"))
                .build();

        var currentProgress = 0;
        final var progressBar = new ProgressBar(20, dataset.trainModels().size() * variations);

        final var ids = new ArrayList<String>((int) (dataset.trainModels().size() * variations));
        for (final var model : dataset.trainModels()) {

            // load 3d-model file
            final var modelFile = InputFileProvider.get(model.path());

            for (int variation = 0; variation < variations; variation++) {
                final var inputFile = randomTransformation(modelFile, TARGET_BOUNDING_BOX);

                // validate file bounds
                if (!TARGET_BOUNDING_BOX.contains(inputFile.getBoundingBox())) {
                    throw new IllegalStateException(INVALID_TRANSFORMATION);
                }

                // voxelize & get label
                final var volume = inputFile.read(TARGET_BOUNDING_BOX);
                final var label = dataset.trainLabels().stream()
                        .filter(l -> l.modelId() == model.id())
                        .findFirst()
                        .orElseThrow();

                // save id
                final var modelId = String.format("%d_%d", model.id(), variation);
                ids.add(modelId);

                // save .vox and label files
                final var voxFileName = String.format("%s/%s.vox", DATASET_VOX, modelId);
                final var txtFileName = String.format("%s/%s.txt", DATASET_VOX, modelId);
                saveVoxFile(voxFileName, volume);
                saveLabelFile(txtFileName, label, inputFile.getBoundingBox());

                ++currentProgress;
                progressBar.printProgress(currentProgress, "Model: " + modelId);
            }
        }

        Collections.shuffle(ids);
        final var splitIndex = (int) (ids.size() * 0.9);
        final var trainIds = ids.subList(0, splitIndex);
        final var valIds = ids.subList(splitIndex, ids.size());

        trainIds.sort(String::compareTo);
        valIds.sort(String::compareTo);

        saveSetFile(DATASET_VOX + "/train.txt", trainIds);
        saveSetFile(DATASET_VOX + "/val.txt", valIds);
        saveCategoriesFile(DATASET_VOX + "/categories.txt", dataset.categories());
    }
}