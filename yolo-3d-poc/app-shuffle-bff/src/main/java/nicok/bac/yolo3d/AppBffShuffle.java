package nicok.bac.yolo3d;

import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.storage.bff.BffReaderRAF;
import nicok.bac.yolo3d.storage.bff.BffWriter;
import org.apache.commons.cli.Options;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.terminal.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.util.DirectoryUtil.requireExtension;

public class AppBffShuffle {

    public static final Options OPTIONS = new Options()
            .addRequiredOption("i", "input", true, "Input file.")
            .addOption("h", "help", false, "Display this help message");

    /**
     * Usage: :app-mess-up-bff-file:run --args='-i C:/src/bac/dataset-psb/db/0/m5/m5.bff'
     */
    public static void main(final String[] args) throws Exception {
        final var commandLine = parseCommandLine("AppBffShuffle", args, OPTIONS);
        final var inputPath = commandLine.getOptionValue("input");

        requireNonNull(inputPath);
        requireExtension(inputPath, ".bff");

        final var vertices = new HashMap<Long, Vertex>();
        final var faces = new HashMap<Long, Face>();

        try (final var bffReader = new BffReaderRAF(inputPath, 10000, 10000)) {
            System.out.println("Reading");

            final var header = bffReader.header();
            System.out.println(header);

            for (long vertexIndex = 0; vertexIndex < header.vertexCount(); vertexIndex++) {
                final var vertex = bffReader.getVertex(vertexIndex);
                vertices.put(vertexIndex, vertex);
            }

            for (long faceIndex = 0; faceIndex < header.faceCount(); faceIndex++) {
                final var face = bffReader.getFace(faceIndex);
                faces.put(faceIndex, face);
            }


            final var newVertexOrder = LongStream.range(0, vertices.size())
                    .boxed()
                    .collect(Collectors.toList());
            final var newFaceOrder = LongStream.range(0, faces.size())
                    .boxed()
                    .collect(Collectors.toList());

            Collections.shuffle(newVertexOrder);
            Collections.shuffle(newFaceOrder);

            try (final var bffWriter = new BffWriter(
                    inputPath.replace(".bff", ".shuffled.bff"),
                    header.precisionBytes(),
                    header.indexBytes()
            )) {
                for (long vertexIndex = 0; vertexIndex < vertices.size(); vertexIndex++) {
                    final var vertex = vertices.get(newVertexOrder.get((int) vertexIndex));
                    bffWriter.writeVertex(vertex);
                }

                // TODO: does not support large files but its low priority
                for (long faceIndex = 0; faceIndex < faces.size(); faceIndex++) {
                    final var originalFace = faces.get(newFaceOrder.get((int) faceIndex));
                    final var shuffledFace = new Face(List.of(
                            newVertexOrder.get(originalFace.vertexIndices().get(0).intValue()),
                            newVertexOrder.get(originalFace.vertexIndices().get(1).intValue()),
                            newVertexOrder.get(originalFace.vertexIndices().get(2).intValue())
                    ));

                    bffWriter.writeFace(shuffledFace);
                }

                bffWriter.writeHeader();
            }
        }

        System.out.println("DONE");
    }
}
