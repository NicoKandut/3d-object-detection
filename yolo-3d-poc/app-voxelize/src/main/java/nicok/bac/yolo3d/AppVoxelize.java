package nicok.bac.yolo3d;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.inputfile.InputFileProvider;
import org.apache.commons.cli.Options;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.util.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.util.CommandLineUtil.parsePoint;
import static nicok.bac.yolo3d.util.DirectoryUtil.getFilename;
import static nicok.bac.yolo3d.vox.VoxFileUtil.saveVoxFile;

public class AppVoxelize {
    public static final Options OPTIONS = new Options()
            .addRequiredOption("i", "input", true, "Input file.")
            .addRequiredOption("o", "output", true, "Output file.")
            .addRequiredOption("s", "size", true, "Size of the output file in voxels, format: x,y,z");

    public static void main(String[] args) throws Exception {
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var inputPath = commandLine.getOptionValue("input");
        final var outputPath = commandLine.getOptionValue("output");
        final var size = parsePoint(commandLine, "size");

        requireNonNull(inputPath);
        requireNonNull(outputPath);

        final var targetBoundingBox = BoundingBox.fromOrigin(size);

        final var inputFile = InputFileProvider.get(inputPath);
        System.out.printf("File bounds: %s\n", inputFile.getBoundingBox());

        System.out.println("Reading volume data");
        final var volume = inputFile.read(targetBoundingBox);

        System.out.println("Saving VOX file");
        final var inputFileName = getFilename(inputPath);
        final var outputFileName = outputPath + "/" + inputFileName + ".vox";

        System.out.printf("  Filename: %s\n", outputFileName);
        System.out.printf("  Volume:   %s\n", targetBoundingBox);
        saveVoxFile(outputFileName, volume);

        System.out.println("DONE");
    }
}
