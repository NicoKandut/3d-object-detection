package nicok.bac.yolo3d.util;

import nicok.bac.yolo3d.off.Vertex;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.util.Pair;

import static java.lang.Double.parseDouble;
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
}
