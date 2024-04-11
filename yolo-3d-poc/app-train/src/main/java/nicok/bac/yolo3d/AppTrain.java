package nicok.bac.yolo3d;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.dataset.Model;
import nicok.bac.yolo3d.dataset.PsbDataset;
import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.terminal.ProgressBar;
import nicok.bac.yolo3d.vox.VoxFileUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.stream.Collectors;

import static java.lang.Runtime.getRuntime;
import static nicok.bac.yolo3d.dataset.VoxDatasetUtils.*;
import static nicok.bac.yolo3d.preprocessing.RandomTransformation.randomTransformation;
import static nicok.bac.yolo3d.terminal.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.terminal.CommandLineUtil.parseOptionalInt;
import static nicok.bac.yolo3d.util.RepositoryPaths.*;

public final class AppTrain {

    public static final Options OPTIONS = new Options()
            .addOption("e", "epochs", true, "Number of training epochs")
            .addOption("se", "super-epochs", true, "Number of training set generations")
            .addOption("pd", "prepare-dataset", true, "Regenerate dataset .vox files");
    public static final int INPUT_SIZE = 112;
    public static final BoundingBox TARGET_BOUNDING_BOX = BoundingBox.fromOrigin(INPUT_SIZE);

    public static void main(final String[] args) throws Exception {

        // cli parsing
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var epochs = parseOptionalInt(commandLine, "epochs", 1);
        final var superEpochs = parseOptionalInt(commandLine, "super-epochs", 1);
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

            // validate file bounds
            if (!TARGET_BOUNDING_BOX.contains(inputFile.getBoundingBox())) {
                throw new IllegalStateException(
                        "This should not happen. There might be a bug in the randomTransformation method."
                );
            }

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

    private static boolean getPrepareDataset(final CommandLine commandLine) {
        final var stringValue = commandLine.getOptionValue("prepare-dataset", "true");
        return Boolean.parseBoolean(stringValue);
    }
}