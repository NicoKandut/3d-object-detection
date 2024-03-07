package nicok.bac.yolo3d;

import nicok.bac.yolo3d.bff.BffWriter;
import nicok.bac.yolo3d.off.OffReader;
import nicok.bac.yolo3d.util.DirectoryUtil;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.util.CommandLineUtil.parseCommandLine;

public class AppOffToBff {

    public static final Options OPTIONS = new Options()
            .addRequiredOption("i", "input", true, "Input file.")
            .addRequiredOption("o", "output", true, "Output file.");

    /**
     * Usage: :app-off-to-bff:run --args='-i C:/src/bac/dataset-psb/db/0/m5/m5.off -o .'
     */
    public static void main(final String[] args) throws Exception {
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var inputPath = commandLine.getOptionValue("input");
        final var outputTarget = commandLine.getOptionValue("output");

        requireNonNull(inputPath);
        requireNonNull(outputTarget);
        requireExtension(inputPath, ".off");

        final var outputPath = getOutputPath(outputTarget, inputPath);
        requireExtension(outputPath, ".bff");

        System.out.println("Converting .off file to .bff");
        try (
                final var offReader = new OffReader(inputPath);
                final var bffWriter = new BffWriter(outputPath)
        ) {
            final var offHeader = offReader.readHeader();
            offReader.readVertices(bffWriter::writeVertex);
            offReader.readFaces(bffWriter::writeFace);
            bffWriter.writeHeader();

            final var bffHeader = bffWriter.getHeader();

            System.out.println("[OFF]: " + inputPath);
            System.out.println("  - " + offHeader);
            System.out.println("[BFF]: " + outputPath);
            System.out.println("  - " + bffHeader);
        }

        System.out.println("Saved");
    }

    private static void requireExtension(final String path, final String extension) {
        if (!path.endsWith(extension)) {
            throw new IllegalArgumentException("File must be of type .off");
        }
    }

    private static String getOutputPath(
            final String outputPath,
            final String inputPath
    ) throws IOException {
        var outputFile = new File(outputPath);
        if (outputFile.isDirectory()) {
            final var filename = DirectoryUtil.getFilename(inputPath);
            outputFile = new File(outputPath + "/" + filename + ".bff");
        } else if (!outputFile.exists()) {
            final var created = outputFile.createNewFile();
            if (!created) {
                throw new IllegalStateException("Failed to create file: " + outputPath);
            }
        }
        return outputFile.getPath();
    }
}
