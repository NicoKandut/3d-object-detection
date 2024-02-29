package nicok.bac.yolo3d.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
}
