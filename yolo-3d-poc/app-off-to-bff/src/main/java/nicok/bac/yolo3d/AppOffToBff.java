package nicok.bac.yolo3d;

import nicok.bac.yolo3d.storage.bff.BffWriter;
import nicok.bac.yolo3d.storage.off.OffReader;
import nicok.bac.yolo3d.util.DirectoryUtil;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.IOException;

import static nicok.bac.yolo3d.terminal.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.util.DirectoryUtil.requireExtension;
import static nicok.bac.yolo3d.util.StringUtil.requireNonBlank;

public class AppOffToBff {

    public static final Options OPTIONS = new Options()
            .addRequiredOption("i", "input", true, "Input file.")
            .addRequiredOption("o", "output", true, "Output file.");

    public static void main(final String[] args) throws Exception {
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var inputPath = commandLine.getOptionValue("input");
        final var outputTarget = commandLine.getOptionValue("output");

        requireNonBlank(inputPath);
        requireNonBlank(outputTarget);
        requireExtension(inputPath, ".off");

        final var outputPath = getOutputPath(outputTarget, inputPath);
        requireExtension(outputPath, ".bff");

        System.out.println("Converting .off file to .bff");
        try (final var offReader = new OffReader(inputPath)) {
            final var offHeader = offReader.readHeader();

            final var precisionBytes = 4;
            final var indexBytes = offHeader.vertexCount() > Integer.MAX_VALUE ? 8 : 4;

            try(final var bffWriter = new BffWriter(outputPath, precisionBytes, indexBytes)) {
                offReader.readVertices(bffWriter::writeVertex);
                offReader.readFaces(bffWriter::writeFace);
                bffWriter.writeHeader();

                final var bffHeader = bffWriter.getHeader();

                System.out.println("[OFF]: " + inputPath);
                System.out.println("  - " + offHeader);
                System.out.println("[BFF]: " + outputPath);
                System.out.println("  - " + bffHeader);
            }
        }

        System.out.println("Saved");
    }

    private static String getOutputPath(
            final String outputPath,
            final String inputPath
    ) throws IOException {
        var outputFile = new File(outputPath);
        if (outputFile.isDirectory()) {
            final var filename = DirectoryUtil.getFilename(inputPath);
            outputFile = new File(outputPath + "/" + filename + ".bff");
        }
        if (!outputFile.exists()) {
            final var created = outputFile.createNewFile();
            if (!created) {
                throw new IllegalStateException("Failed to create file: " + outputPath);
            }
        }
        return outputFile.getPath();
    }
}
