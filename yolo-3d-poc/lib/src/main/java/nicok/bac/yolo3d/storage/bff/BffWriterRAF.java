package nicok.bac.yolo3d.storage.bff;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.storage.BinaryWriter;

import java.io.IOException;
import java.io.RandomAccessFile;

import static java.util.Objects.requireNonNull;

public final class BffWriterRAF implements AutoCloseable {

    private final RandomAccessFile file;
    private final BoundingBox.Builder boundingBox = new BoundingBox.Builder();
    private final long precisionBytes;
    private final long indexBytes;
    private long vertexCount = 0;
    private long faceCount = 0;

    public BffWriterRAF(
            final String path,
            final int precisionBytes,
            final int indexBytes
    ) throws IOException {
        requireNonNull(path);
        this.precisionBytes = precisionBytes;
        this.indexBytes = indexBytes;
        this.file = new RandomAccessFile(path, "rw");
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
        file.seek(0);
        final var header = getHeader();
        BinaryWriter.write(file, header);
    }

    public void writeVertex(final Vertex vertex) throws IOException {
        file.seek(BffFormat.getVertexPosition(vertexCount, precisionBytes));
        BinaryWriter.write(file, vertex, precisionBytes);
        boundingBox.withVertex(vertex);
        vertexCount += 1;
    }

    public void writeFace(final Face face, final long totalVertexCount) throws IOException {
        if (face.vertexIndices().size() != 3) {
            throw new IllegalArgumentException("Face must have 3 vertices");
        }
        file.seek(BffFormat.getFacePosition(faceCount, precisionBytes, indexBytes, totalVertexCount));
        BinaryWriter.write(file, face, indexBytes);
        faceCount += 1;
    }

    @Override
    public void close() throws Exception {
        if (this.file != null) {
            this.file.close();
        }
    }
}
