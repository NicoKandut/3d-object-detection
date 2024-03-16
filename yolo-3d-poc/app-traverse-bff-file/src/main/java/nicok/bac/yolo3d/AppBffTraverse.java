package nicok.bac.yolo3d;

import nicok.bac.yolo3d.bff.BffReader;
import org.apache.commons.cli.Options;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.util.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.util.DirectoryUtil.requireExtension;
import static nicok.bac.yolo3d.util.StringUtil.requireNonBlank;

public class AppBffTraverse {

    public static final Options OPTIONS = new Options()
            .addRequiredOption("i", "input", true, "Input file.");

    /**
     * Usage: :app-traverse-bff-file:run --args='-i C:/src/bac/dataset-psb/db/0/m5/m5.off'
     */
    public static void main(final String[] args) throws Exception {
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var inputPath = commandLine.getOptionValue("input");

        requireNonBlank(inputPath);
        requireExtension(inputPath, ".bff");

        System.out.println("Correlating Faces And Vertices");
        try (final var bffReader = new BffReader(inputPath)) {

            // read header
            final var header = bffReader.readHeader();
            System.out.println(header);

            // iterate faces and vertices
            bffReader.getFaces()
                    .flatMap(face -> face.vertexIndices().stream())
                    .forEach(bffReader::getVertex);

            final var vertexCacheHits = bffReader.vertexCacheHits();
            final var vertexRepeatAccesses = bffReader.vertexCacheQueries() - header.vertexCount();
            final var vertexRatio = (double) vertexCacheHits / (double) vertexRepeatAccesses * 100.0;
            System.out.printf("Vertex cache hits: %d / %d. (%.2f%%)\n", vertexCacheHits, vertexRepeatAccesses, vertexRatio);
        }
    }
}
