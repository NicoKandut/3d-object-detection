package nicok.bac.yolo3d.storage.bff;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.Vertex;

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
        final var header = getHeader();
        file.seek(0);
        file.write(BffFormat.FILE_TYPE.getBytes());
        file.writeByte((int) header.precisionBytes());
        file.writeByte((int) header.indexBytes());
        file.writeLong(header.vertexCount());
        file.writeLong(header.faceCount());
        file.writeLong(header.edgeCount());
        final var boundingBox = this.boundingBox.build();
        file.writeDouble(boundingBox.min().x());
        file.writeDouble(boundingBox.min().y());
        file.writeDouble(boundingBox.min().z());
        file.writeDouble(boundingBox.max().x());
        file.writeDouble(boundingBox.max().y());
        file.writeDouble(boundingBox.max().z());
    }

    public void writeVertex(final Vertex vertex) throws IOException {
        file.seek(BffFormat.getVertexPosition(vertexCount, precisionBytes));

        if (precisionBytes == 4) {
            file.writeFloat((float) vertex.x());
            file.writeFloat((float) vertex.y());
            file.writeFloat((float) vertex.z());
        } else {
            file.writeDouble(vertex.x());
            file.writeDouble(vertex.y());
            file.writeDouble(vertex.z());
        }

        boundingBox.withVertex(vertex);
        vertexCount += 1;
    }

    // TODO: use Triangle instead of face?
    public void writeFace(final Face face, final long totalVertexCount) throws IOException {
        if (face.vertexIndices().size() != 3) {
            throw new IllegalArgumentException("Face must have 3 vertices");
        }

        file.seek(BffFormat.getFacePosition(faceCount, precisionBytes, indexBytes, totalVertexCount));
        if (indexBytes == 4) {
            for (final var index : face.vertexIndices()) {
                file.writeInt(index.intValue());
            }
        } else {
            for (final var index : face.vertexIndices()) {
                file.writeLong(index);
            }
        }

        faceCount += 1;
    }

    @Override
    public void close() throws Exception {
        if (this.file != null) {
            this.file.close();
        }
    }
}
