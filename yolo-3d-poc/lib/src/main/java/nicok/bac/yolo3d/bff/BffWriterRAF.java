package nicok.bac.yolo3d.bff;

import nicok.bac.yolo3d.off.Face;
import nicok.bac.yolo3d.off.Header;
import nicok.bac.yolo3d.off.Vertex;
import nicok.bac.yolo3d.util.Triangulation;

import java.io.IOException;
import java.io.RandomAccessFile;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.bff.BffFormat.*;

public final class BffWriterRAF implements AutoCloseable {

    private final RandomAccessFile file;
    private final String path;
    private int vertexCount = 0;
    private int faceCount = 0;

    public BffWriterRAF(final String path) throws IOException {
        this.path = requireNonNull(path);
        this.file = new RandomAccessFile(path, "rw");
    }

    public Header getHeader() {
        return new Header(
                vertexCount,
                faceCount,
                vertexCount + faceCount - 2
        );
    }

    public void writeHeader() throws IOException {
        final var header = getHeader();
        file.seek(0);
        file.write(FILE_TYPE.getBytes());
        file.writeInt(header.vertexCount());
        file.writeInt(header.faceCount());
        file.writeInt(header.edgeCount());
    }

    public void writeVertex(final Vertex vertex) throws IOException {
        file.seek(getVertexPosition(vertexCount));
        file.writeDouble(vertex.x());
        file.writeDouble(vertex.y());
        file.writeDouble(vertex.z());
        vertexCount += 1;
    }

    public void writeFace(final Face face, final long totalVertexCount) throws IOException {
        final var count = face.vertexIndices().size();
        file.seek(getFacePosition(faceCount, totalVertexCount));
        if (count == 3) {
            for (final var index : face.vertexIndices()) {
                file.writeInt(index);
            }
        } else {
            final var faces = Triangulation.shell(face);
            for (final var triangle : faces) {
                for (final var index : triangle.vertexIndices()) {
                    file.writeInt(index);
                }
            }
        }
        faceCount += count - 2;
    }

    @Override
    public void close() throws Exception {
        this.file.close();
    }
}
