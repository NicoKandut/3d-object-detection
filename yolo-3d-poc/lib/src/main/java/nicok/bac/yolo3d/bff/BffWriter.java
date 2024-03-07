package nicok.bac.yolo3d.bff;

import nicok.bac.yolo3d.off.Face;
import nicok.bac.yolo3d.off.Header;
import nicok.bac.yolo3d.off.Vertex;
import nicok.bac.yolo3d.util.Triangulation;

import java.io.*;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.bff.BffFormat.FILE_TYPE;
import static nicok.bac.yolo3d.bff.BffFormat.HEADER_BYTES;

public final class BffWriter implements AutoCloseable {

    private final DataOutputStream stream;
    private final String path;
    private int vertexCount = 0;
    private int faceCount = 0;

    public BffWriter(final String path) throws IOException {
        this.path = requireNonNull(path);
        this.stream = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(path)
                )
        );

        final var headerPlaceholder = new byte[HEADER_BYTES];
        this.stream.write(headerPlaceholder);
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
        try (final var file = new RandomAccessFile(path,"rw")) {
            file.write(FILE_TYPE.getBytes());
            file.writeInt(header.vertexCount());
            file.writeInt(header.faceCount());
            file.writeInt(header.edgeCount());
        }
    }

    public void writeVertex(final Vertex vertex) throws IOException {
        stream.writeDouble(vertex.x());
        stream.writeDouble(vertex.y());
        stream.writeDouble(vertex.z());
        vertexCount += 1;
    }

    public void writeFace(final Face face) throws IOException {
        final var count = face.vertexIndices().size();

        if (count == 3) {
            for (final var index : face.vertexIndices()) {
                stream.writeInt(index);
            }
        } else {
            final var faces = Triangulation.shell(face);
            for (final var triangle : faces) {
                for (final var index : triangle.vertexIndices()) {
                    stream.writeInt(index);
                }
            }
        }

        faceCount += count - 2;
    }

    @Override
    public void close() throws Exception {
        this.stream.close();
    }
}
