package nicok.bac.yolo3d.storage.bff;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.storage.BinaryWriter;
import nicok.bac.yolo3d.storage.OrderedMeshWriter;

import java.io.*;

import static java.util.Objects.requireNonNull;

/**
 * A writer for the BFF file format.
 * Designed to efficiently write a file sequentially and in order.
 */
public final class BffWriter implements AutoCloseable, OrderedMeshWriter {

    private static final byte[] HEADER_PLACEHOLDER = new byte[(int) BffFormat.HEADER_BYTES];

    private final String path;
    private final DataOutputStream stream;
    private final BoundingBox.Builder boundingBox = new BoundingBox.Builder();
    private final long precisionBytes;
    private final long indexBytes;
    private long vertexCount = 0;
    private long faceCount = 0;

    public BffWriter(
            final String path,
            final long precisionBytes,
            final long indexBytes
    ) throws IOException {
        this.path = requireNonNull(path);
        this.precisionBytes = precisionBytes;
        this.indexBytes = indexBytes;
        this.stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
        this.stream.write(HEADER_PLACEHOLDER);
    }

    public BffHeader getHeader() {
        return new BffHeader(
                precisionBytes,
                indexBytes,
                vertexCount,
                faceCount,
                vertexCount + faceCount - 2,
                boundingBox.build()
        );
    }

    public void writeHeader() throws IOException {
        this.stream.flush();
        final var header = getHeader();
        final var headerStream = new RandomAccessFile(path, "rw");
        headerStream.seek(0);
        BinaryWriter.write(headerStream, header);
        headerStream.close();
    }

    @Override
    public void writeVertex(final Vertex vertex) throws IOException {
        BinaryWriter.write(stream, vertex, precisionBytes);
        boundingBox.withVertex(vertex);
        vertexCount += 1;
    }

    @Override
    public void writeFace(final Face face) throws IOException {
        final var count = face.vertexIndices().size();
        if (count != 3) {
            throw new IllegalArgumentException("Face must have 3 vertices");
        }
        BinaryWriter.write(stream, face, indexBytes);
        faceCount += count - 2;
    }

    @Override
    public void close() throws Exception {
        if (this.stream != null) {
            this.stream.close();
        }
    }
}
