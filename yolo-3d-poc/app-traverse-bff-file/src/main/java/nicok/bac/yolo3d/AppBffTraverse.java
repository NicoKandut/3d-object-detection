package nicok.bac.yolo3d;

import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.storage.bff.BffReaderRAF;
import org.apache.commons.cli.Options;

import static nicok.bac.yolo3d.terminal.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.util.DirectoryUtil.requireExtension;
import static nicok.bac.yolo3d.util.StringUtil.requireNonBlank;

public class AppBffTraverse {

    public static final Options OPTIONS = new Options()
            .addRequiredOption("i", "input", true, "Input file.")
            .addOption("h", "help", false, "Display this help message");

    /**
     * Usage: :app-traverse-bff-file:run --args='-i C:/src/bac/dataset-psb/db/0/m5/m5.off'
     */
    public static void main(final String[] args) throws Exception {
        final var commandLine = parseCommandLine("AppBffTraverse", args, OPTIONS);
        final var inputPath = commandLine.getOptionValue("input");

        requireNonBlank(inputPath, "input path must not be empty");
        requireExtension(inputPath, ".bff");

        System.out.println("Correlating Faces And Vertices");
        try (final var bffReader = new BffReaderRAF(inputPath)) {

            // read header
            final var header = bffReader.header();
            System.out.println(header);

            // iterate faces and vertices
            bffReader.getFaces()
                    .flatMap(face -> face.vertexIndices().stream())
                    .forEach(bffReader::getVertex);

            bffReader.vertices()
                    .map(Vertex::z)
                    .forEach(System.out::println);

            bffReader.printStatistic();
        }
    }
}
