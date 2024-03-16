package nicok.bac.yolo3d;

import nicok.bac.yolo3d.bff.BffReader;
import nicok.bac.yolo3d.bff.BffWriter;
import nicok.bac.yolo3d.off.Face;
import nicok.bac.yolo3d.off.Vertex;
import org.apache.commons.cli.Options;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.util.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.util.DirectoryUtil.requireExtension;

public class AppBffShuffle {

    public static final Options OPTIONS = new Options()
            .addRequiredOption("i", "input", true, "Input file.");

    /**
     * Usage: :app-mess-up-bff-file:run --args='-i C:/src/bac/dataset-psb/db/0/m5/m5.bff'
     */
    public static void main(final String[] args) throws Exception {
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var inputPath = commandLine.getOptionValue("input");

        requireNonNull(inputPath);
        requireExtension(inputPath, ".bff");

        final var vertices = new HashMap<Integer, Vertex>();
        final var faces = new HashMap<Integer, Face>();

        try (final var bffReader = new BffReader(inputPath, 10000, 10000)) {
            System.out.println("Reading");

            final var header = bffReader.readHeader();
            System.out.println(header);

            for (int vertexIndex = 0; vertexIndex < header.vertexCount(); vertexIndex++) {
                final var vertex = bffReader.getVertex(vertexIndex);
                vertices.put(vertexIndex, vertex);
            }

            for (int faceIndex = 0; faceIndex < header.faceCount(); faceIndex++) {
                final var face = bffReader.getFace(faceIndex);
                faces.put(faceIndex, face);
            }
        }

        final var newVertexOrder = IntStream.range(0, vertices.size())
                .boxed()
                .collect(Collectors.toList());
        final var newFaceOrder = IntStream.range(0, faces.size())
                .boxed()
                .collect(Collectors.toList());

        Collections.shuffle(newVertexOrder);
        Collections.shuffle(newFaceOrder);

        try (final var bffWriter = new BffWriter(inputPath.replace(".bff", ".fucked.bff"))) {
            for (int vertexIndex = 0; vertexIndex < vertices.size(); vertexIndex++) {
                final var vertex = vertices.get(newVertexOrder.get(vertexIndex));
                bffWriter.writeVertex(vertex);
            }

            for (int faceIndex = 0; faceIndex < faces.size(); faceIndex++) {
                final var originalFace = faces.get(newFaceOrder.get(faceIndex));
                final var shuffledFace = new Face(List.of(
                        newVertexOrder.get(originalFace.vertexIndices().get(0)),
                        newVertexOrder.get(originalFace.vertexIndices().get(1)),
                        newVertexOrder.get(originalFace.vertexIndices().get(2))
                ));

                bffWriter.writeFace(shuffledFace);
            }

            bffWriter.writeHeader();
        }

        System.out.println("DONE");
    }
}
