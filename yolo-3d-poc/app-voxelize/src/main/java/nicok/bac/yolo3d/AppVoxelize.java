package nicok.bac.yolo3d;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.inputfile.InputFile;
import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.preprocessing.FitToBox;
import nicok.bac.yolo3d.storage.chunkstore.ChunkStore;
import org.apache.commons.cli.Options;

import static nicok.bac.yolo3d.terminal.CommandLineUtil.*;
import static nicok.bac.yolo3d.util.StringUtil.requireNonBlank;
import static nicok.bac.yolo3d.vox.VoxFileUtil.saveVoxFile;

public class AppVoxelize {
    public static final Options OPTIONS = new Options()
            .addOption("i", "input", true, "Input file.")
            .addOption("s", "size", true, "Size of the output file in voxels, format: x,y,z")
            .addOption("f", "fit", false, "Fit to bounding box. Default: false.")
            .addOption("h", "help", false, "Display this help message")
            .addOption("v", "vox", true, "Save VOX file");

    public static void main(String[] args) throws Exception {
        try {
            final var commandLine = parseCommandLine("AppVoxelize", args, OPTIONS);
            final var inputPath = commandLine.getOptionValue("input");
            final var voxPath = commandLine.getOptionValue("vox");
            final var size = parsePoint(commandLine, "size", "size argument must not be empty");
            final var fit = commandLine.hasOption("fit");

            requireNonBlank(inputPath, "input path must not be empty");

            final var targetBoundingBox = BoundingBox.fromOrigin(size);

            var inputFile = InputFileProvider.get(inputPath);
            System.out.printf("File bounds: %s\n", inputFile.getBoundingBox());

            if (fit) {
                inputFile = fitToBox(inputFile, targetBoundingBox);
            }

            System.out.println("Reading volume data");
            final var chunkStore = inputFile.createChunkStore();
            chunkStore.writeHeaderFile();

            if(voxPath != null && !voxPath.isBlank()) {
                writeChunkStoreToVox(inputPath, voxPath, chunkStore);
            }
        } catch (final Exception exception) {
            System.out.println("Error: " + exception.getMessage());
            throw exception;
        }
    }

    private static InputFile fitToBox(InputFile inputFile, BoundingBox targetBoundingBox) {
        System.out.println("Fitting to bounding box");
        final var transform = new FitToBox()
                .withSourceBoundingBox(inputFile.getBoundingBox())
                .withTargetBoundingBox(targetBoundingBox);
        inputFile = inputFile.transform(transform);
        System.out.printf("File bounds: %s\n", inputFile.getBoundingBox());
        return inputFile;
    }

    private static void writeChunkStoreToVox(String inputPath, String outputPath, ChunkStore chunkStore) {
        System.out.println("Saving VOX file");
        System.out.printf("  Filename: %s\n", outputPath);
        System.out.printf("  Volume:   %s\n", chunkStore.boundingBox());
        saveVoxFile(outputPath, chunkStore);

        chunkStore.printStatistic();

        System.out.println("DONE");
    }
}
