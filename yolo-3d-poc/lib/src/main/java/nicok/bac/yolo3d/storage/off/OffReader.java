package nicok.bac.yolo3d.storage.off;

import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.util.ThrowingConsumer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.StringTokenizer;

import static java.util.Objects.requireNonNull;

/**
 * A reader for the OFF file format.
 * This reader is designed to read the entire file into memory.
 * To handle large files, consider using the BFF file format.
 */
public final class OffReader implements AutoCloseable {

    public static final String DELIMITER = " \n\t";

    private final String path;
    private BufferedReader reader;
    private OffHeader header;

    public OffReader(final String path) {
        this.path = requireNonNull(path);
    }

    /**
     * Reads the entire file into an OffMesh object.
     *
     * @return a fully populated OffMesh
     * @throws IOException when an underlying I/O operation fails
     */
    public OffMesh readMesh() throws IOException {
        final var fileInfo = this.readHeader();
        final var meshBuilder = new OffMesh.Builder(fileInfo.vertexCount(), fileInfo.faceCount());

        this.readVertices(meshBuilder::addVertex);
        this.readFaces(meshBuilder::addFace);

        return meshBuilder.build();
    }

    public OffHeader readHeader() throws IOException {
        reader = new BufferedReader(new FileReader(path));

        // line 1 - OFF
        final var off_line = readIgnoreComments(reader);
        if (!Objects.equals(off_line, "OFF")) {
            throw new IllegalStateException(".off files should start with OFF");
        }

        // line 2 - vertexCount faceCount edgeCount
        final var head_line = readIgnoreComments(reader);
        final var tokenStream = new StringTokenizer(head_line, DELIMITER);
        final var vertexCount = Integer.parseInt(tokenStream.nextToken());
        final var faceCount = Integer.parseInt(tokenStream.nextToken());
        final var edgeCount = Integer.parseInt(tokenStream.nextToken());

        header = new OffHeader(vertexCount, faceCount, edgeCount);

        return header;
    }

    public void readVertices(final ThrowingConsumer<Vertex, IOException> onVertex) throws IOException {
        for (var i_vertex = 0; i_vertex < header.vertexCount(); ++i_vertex) {
            final var vertex_line = readIgnoreComments(reader);
            final var tokenStream = new StringTokenizer(vertex_line, DELIMITER);
            final var x = Double.parseDouble(tokenStream.nextToken());
            final var y = Double.parseDouble(tokenStream.nextToken());
            final var z = Double.parseDouble(tokenStream.nextToken());

            // switch coordinates because .off uses y as vertical axis
            onVertex.accept(new Vertex(x, z, y));
        }
    }

    public void readFaces(final ThrowingConsumer<Face, IOException> onFace) throws IOException {
        for (var i_faces = 0; i_faces < header.faceCount(); ++i_faces) {
            final var face_line = readIgnoreComments(reader);
            final var tokenStream = new StringTokenizer(face_line, DELIMITER);
            final var faceVertexIndexCount = Long.parseLong(tokenStream.nextToken());
            final var faceVertexIndices = new ArrayList<Long>();
            for (var i_vertex = 1; i_vertex <= faceVertexIndexCount; ++i_vertex) {
                final var index = Long.parseLong(tokenStream.nextToken());
                faceVertexIndices.add(index);
            }
            onFace.accept(new Face(faceVertexIndices));
        }
    }

    private static String readIgnoreComments(final BufferedReader reader) throws IOException {
        var line = reader.readLine().trim();
        while (line.startsWith("#")) {
            line = reader.readLine();
        }
        return line;
    }

    @Override
    public void close() throws Exception {
        if (reader != null) {
            reader.close();
        }
    }
}
