package nicok.bac.yolo3d.off;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OffFile implements AutoCloseable {

    private final String path;
    private final BufferedReader reader;
    private final Header header;

    private final long headerLines;
    private long lineVertex0 = 0;
    private long lineFace0 = 0;

    private final Map<Long, Vertex> vertexCache;
    private final Map<Long, Face> faceCache;

    public OffFile(final String path) throws Exception {
        this.path = path;
        this.reader = new BufferedReader(new FileReader(this.path));
        this.header = readHeader();
        this.headerLines = 2;

        this.vertexCache = new HashMap<>(100);
        this.faceCache = new HashMap<>(100);
    }

    private Header readHeader()  throws IOException, IllegalStateException {
        if (this.reader == null) {
            throw new IllegalStateException("Cannot read header without reader");
        }

        // line 1 - OFF
        final var first_line = this.readLineIgnoreComments();
        throwOnInvalidOffLine(first_line);

        // line 2 - vertexCount faceCount edgeCount
        final var head_line = this.readLineIgnoreComments();
        final var head_parts = head_line.split("\\s+");
        if (head_parts.length < 3) {
            throw new IllegalStateException(
                    "File did not contain the number of vertices, number of faces, and number of edges"
            );
        }

        try {
            final var vertexCount = Integer.parseInt(head_parts[0]);
            final var faceCount = Integer.parseInt(head_parts[1]);
            final var edgeCount = Integer.parseInt(head_parts[2]);
            return new Header(vertexCount, faceCount, edgeCount);
        } catch (final NumberFormatException exception) {
            throw new IllegalStateException(
                    "Failed to parse  the number of vertices, number of faces, and number of edges."
            );
        }
    }

    public Vertex getVertex(final long index) {
        return null;
    }

    private static void throwOnInvalidOffLine(final String off_line)  {
        if (off_line == null) {
            throw new IllegalStateException("Seems like .off file had no lines");
        }

        if (!off_line.trim().equals("OFF")) {
            throw new IllegalStateException(
                    String.format(".off files should start with OFF in the first line. Got \"%s\".", off_line)
            );
        }
    }

    private String readLineIgnoreComments() throws IOException {
        var line = this.reader.readLine();
        while (line.trim().startsWith("#")) {
            line = this.reader.readLine();
        }
        return line;
    }

    @Override
    public void close() throws Exception {
        reader.close();
    }
}
