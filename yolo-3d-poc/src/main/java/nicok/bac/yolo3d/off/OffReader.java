package nicok.bac.yolo3d.off;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

public final class OffReader implements AutoCloseable {
    private final String path;
    private final double scaling;
    private BufferedReader reader;
    private Header header;

    public OffReader(
            final String path,
            final double scaling
    ) {
        this.path = Objects.requireNonNull(path);
        this.scaling = scaling;
    }

    private static <T> void noop(final T ignored) {
    }

    public Header readHeader() throws IOException {
        reader = new BufferedReader(new FileReader(path));

        final var off_line = readIgnoreComments(reader);
        if (!Objects.equals(off_line, "OFF")) {
            throw new IllegalStateException(".off files should start with OFF");
        }

        // line 2 - vertexCount faceCount edgeCount
        final var head_line = readIgnoreComments(reader);
        final var head_parts = head_line.split("\\s+");
        final var vertexCount = Long.parseLong(head_parts[0]);
        final var faceCount = Long.parseLong(head_parts[1]);
        final var edgeCount = Long.parseLong(head_parts[2]);

        header = new Header(vertexCount, faceCount, edgeCount);

        return header;
    }

    public void readVertices(
            final Consumer<Vertex> onVertex
    ) throws IOException {
        // vertices
        for (var i_vertex = 0; i_vertex < header.vertexCount(); ++i_vertex) {
            final var vertex_line = readIgnoreComments(reader);
            final var vertex_parts = vertex_line.split("\\s+");
            final var x = scaling * Double.parseDouble(vertex_parts[0]);
            final var y = scaling * Double.parseDouble(vertex_parts[1]);
            final var z = scaling * Double.parseDouble(vertex_parts[2]);

            if (Objects.nonNull(onVertex)) {
                onVertex.accept(new Vertex(x, y, z));
            }
        }
    }

    public void readFaces(
            final Consumer<Face> onFace
    ) throws IOException {
        // faces
        for (var i_faces = 0; i_faces < header.faceCount(); ++i_faces) {
            final var face_line = readIgnoreComments(reader);
            final var face_parts = face_line.split("\\s+");
            final var faceVertexIndexCount = Long.parseLong(face_parts[0]);

            final var faceVertexIndices = new ArrayList<Long>();
            for (var i_vertex = 1; i_vertex <= faceVertexIndexCount; ++i_vertex) {
                final var index = Long.parseLong(face_parts[i_vertex]);
                faceVertexIndices.add(index);
            }

            if (Objects.nonNull(onFace)) {
                onFace.accept(new Face(faceVertexIndices));
            }
        }
    }


    private static String readIgnoreComments(final BufferedReader reader) throws IOException {
        var line = reader.readLine();
        while (line.trim().startsWith("#")) {
            line = reader.readLine();
        }
        return line;
    }

    @Override
    public void close() throws Exception {
        reader.close();
    }
}
