package nicok.bac.yolo3d;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.inputfile.InputFile;
import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.preprocessing.FitToBox;
import nicok.bac.yolo3d.storage.chunkstore.ChunkStore;
import org.apache.commons.cli.Options;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.terminal.CommandLineUtil.*;
import static nicok.bac.yolo3d.util.DirectoryUtil.getFilename;
import static nicok.bac.yolo3d.util.StringUtil.requireNonBlank;
import static nicok.bac.yolo3d.vox.VoxFileUtil.saveVoxFile;

public class AppVoxelize {
    public static final Options OPTIONS = new Options()
            .addOption("i", "input", true, "Input file.")
            .addOption("o", "output", true, "Output file.")
            .addOption("s", "size", true, "Size of the output file in voxels, format: x,y,z")
            .addOption("f", "fit", true, "Fit to bounding box. Default: false.")
            .addOption("h", "help", false, "Display this help message");

    public static void main(String[] args) throws Exception {
        final var commandLine = parseCommandLine("AppVoxelize", args, OPTIONS);
        final var inputPath = commandLine.getOptionValue("input");
        final var outputPath = commandLine.getOptionValue("output");
        final var size = parsePoint(commandLine, "size");
        final var fit = parseBoolean(commandLine, "fit", false);

        requireNonBlank(inputPath);
        requireNonBlank(outputPath);

        final var targetBoundingBox = BoundingBox.fromOrigin(size);

        var inputFile = InputFileProvider.get(inputPath);
        System.out.printf("File bounds: %s\n", inputFile.getBoundingBox());

        if (fit) {
            inputFile = fitToBox(inputFile, targetBoundingBox);
        }

        System.out.println("Reading volume data");
        final var chunkStore = inputFile.createChunkStore();
        chunkStore.writeHeaderFile();

        writeChunkStoreToVox(inputPath, outputPath, chunkStore);
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
        final var inputFileName = getFilename(inputPath);
        final var outputFileName = outputPath + "/" + inputFileName + ".vox";

        System.out.printf("  Filename: %s\n", outputFileName);
        System.out.printf("  Volume:   %s\n", chunkStore.boundingBox());
        saveVoxFile(outputFileName, chunkStore);

        chunkStore.printStatistic();

        System.out.println("DONE");
    }
}
