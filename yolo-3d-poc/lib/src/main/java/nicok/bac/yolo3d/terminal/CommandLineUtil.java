package nicok.bac.yolo3d.terminal;

import nicok.bac.yolo3d.mesh.Vertex;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.util.Pair;

import static java.lang.Double.parseDouble;
import static nicok.bac.yolo3d.util.DirectoryUtil.requireExtension;
import static nicok.bac.yolo3d.util.StringUtil.requireLength;
import static nicok.bac.yolo3d.util.StringUtil.requireNonBlank;

public final class CommandLineUtil {

    public static CommandLine parseCommandLine(
            final String[] args,
            final Options options
    ) {
        final var parser = new DefaultParser();
        try {
            return parser.parse(options, args);
        } catch (final ParseException exception) {
            System.err.printf("Error: %s", exception.getMessage());
            System.exit(1);
            return null;
        }
    }

    public static Vertex parsePoint(
            final CommandLine commandLine,
            final String argumentName
    ) {
        requireNonBlank(argumentName);

        final var sizeString = commandLine.getOptionValue(argumentName);
        requireNonBlank(sizeString);

        final var parts = sizeString.split(",");
        requireLength(parts, 3);

        final var x = parseDouble(parts[0].trim());
        final var y = parseDouble(parts[1].trim());
        final var z = parseDouble(parts[2].trim());

        return new Vertex(x, y, z);
    }

    public static Pair<Double, Double> parseDoubleRange(
            final CommandLine commandLine,
            final String argumentName
    ) {
        requireNonBlank(argumentName);

        final var sizeString = commandLine.getOptionValue(argumentName);
        requireNonBlank(sizeString);

        final var parts = sizeString.split(",");
        requireLength(parts, 2);

        final var min = parseDouble(parts[0].trim());
        final var max = parseDouble(parts[1].trim());

        return Pair.create(min, max);
    }

    public static int parseOptionalInt(
            final CommandLine commandLine,
            final String argumentName,
            final int defaultValue
    ) {
        final var stringValue = commandLine.getOptionValue(argumentName);

        if (stringValue == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(stringValue);
        } catch (final NumberFormatException e) {
            System.out.printf("Warning: Argument '%s=%s' is invalid and has been discarded.", argumentName, stringValue);
            return defaultValue;
        }
    }

    public static boolean parseBoolean(
            final CommandLine commandLine,
            final String argumentName,
            final boolean defaultValue
    ) {
        final var stringValue = commandLine.getOptionValue(argumentName);

        if (stringValue == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(stringValue);
    }

    public static String parseFilePath(
            final CommandLine commandLine,
            final String argumentName,
            final String... extensions
    ) {
        final var outputPath = commandLine.getOptionValue(argumentName);
        requireExtension(outputPath, extensions);
        return outputPath;
    }
}
