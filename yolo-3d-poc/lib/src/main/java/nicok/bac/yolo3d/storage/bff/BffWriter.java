package nicok.bac.yolo3d.storage.bff;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.Vertex;

import java.io.*;

import static java.util.Objects.requireNonNull;

/**
 * A writer for the BFF file format.
 * Designed to efficiently write a file sequentially and in order.
 */
public final class BffWriter implements AutoCloseable {

    private static final byte[] HEADER_PLACEHOLDER = new byte[(int) BffFormat.HEADER_BYTES];

    private final String path;
    private final DataOutputStream stream;
    private final BoundingBox.Builder boundingBox = new BoundingBox.Builder();
    private long precisionBytes = 0;
    private long indexBytes = 0;
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
        final var header = getHeader();
        try (final var headerStream = new RandomAccessFile(this.path, "rw")) {
            headerStream.write(BffFormat.FILE_TYPE.getBytes());
            headerStream.writeByte((int) header.precisionBytes());
            headerStream.writeByte((int) header.indexBytes());
            headerStream.writeLong(header.vertexCount());
            headerStream.writeLong(header.faceCount());
            headerStream.writeLong(header.edgeCount());
            headerStream.writeDouble(header.boundingBox().min().x());
            headerStream.writeDouble(header.boundingBox().min().y());
            headerStream.writeDouble(header.boundingBox().min().z());
            headerStream.writeDouble(header.boundingBox().max().x());
            headerStream.writeDouble(header.boundingBox().max().y());
            headerStream.writeDouble(header.boundingBox().max().z());
        }
    }

    public void writeVertex(final Vertex vertex) throws IOException {
        if (precisionBytes == 4) {
            stream.writeFloat((float) vertex.x());
            stream.writeFloat((float) vertex.y());
            stream.writeFloat((float) vertex.z());
        } else {
            stream.writeDouble(vertex.x());
            stream.writeDouble(vertex.y());
            stream.writeDouble(vertex.z());
        }

        boundingBox.withVertex(vertex);
        vertexCount += 1;
    }

    public void writeFace(final Face face) throws IOException {
        final var count = face.vertexIndices().size();

        if (count != 3) {
            throw new IllegalArgumentException("Face must have 3 vertices");
        }

        if (indexBytes == 4) {
            for (final var index : face.vertexIndices()) {
                stream.writeInt(index.intValue());
            }
        } else {
            for (final var index : face.vertexIndices()) {
                stream.writeLong(index);
            }
        }

        faceCount += count - 2;
    }

    @Override
    public void close() throws Exception {
        if (this.stream != null) {
            this.stream.close();
        }
    }
}
