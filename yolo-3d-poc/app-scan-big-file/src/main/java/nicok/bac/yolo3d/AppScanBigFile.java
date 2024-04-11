package nicok.bac.yolo3d;

import nicok.bac.yolo3d.inputfile.ChunkStoreAdapter;
import nicok.bac.yolo3d.inputfile.InputFileProvider;
import nicok.bac.yolo3d.network.Yolo3dNetwork;
import nicok.bac.yolo3d.scanner.Scanner;
import nicok.bac.yolo3d.util.RepositoryPaths;
import org.apache.commons.cli.Options;

import static nicok.bac.yolo3d.terminal.CommandLineUtil.parseCommandLine;

public class AppScanBigFile {

    public static final Options OPTIONS = new Options()
            .addRequiredOption("i", "input", true, "Input file.");

    public static void main(final String[] args) throws Exception {

        // parse CLI arguments
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var inputPath = commandLine.getOptionValue("input");

        // init input file
        final var inputFile = InputFileProvider.get(inputPath);

        // load network
        final var network = new Yolo3dNetwork(RepositoryPaths.SAVED_MODEL);

        // scan
        final var scanner = new Scanner();
        final var result = scanner.scan(inputFile, network);

        // refine results
        final var filter = new BoxFilter(0.1, 0.5);
        final var boxes = filter.filter(result.boxes());
        System.out.printf("%d boxes survived\n", boxes.size());

        ((ChunkStoreAdapter) inputFile).stats();

        for (final var box : boxes) {
            System.out.printf("  - %s\n", box);
        }
    }
}
