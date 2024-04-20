package nicok.bac.yolo3d.storage;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.TriangleEvent;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.storage.bff.BffHeader;

import java.io.DataInput;
import java.io.IOException;
import java.util.List;

import static nicok.bac.yolo3d.storage.bff.BffFormat.FILE_TYPE;

/**
 * Reads different types from a binary file.
 */
public final class BinaryReader {

    public static BoundingBox readBoundingBox(final DataInput input) throws IOException {
        final var minX = input.readDouble();
        final var minY = input.readDouble();
        final var minZ = input.readDouble();
        final var maxX = input.readDouble();
        final var maxY = input.readDouble();
        final var maxZ = input.readDouble();
        return new BoundingBox(new Vertex(minX, minY, minZ), new Vertex(maxX, maxY, maxZ));
    }


    public static BffHeader readBffHeader(final DataInput input) throws IOException {
        final var fileType = "%c%c%c%c".formatted(
                input.readByte(),
                input.readByte(),
                input.readByte(),
                input.readByte()
        );
        if (!fileType.equals(FILE_TYPE)) {
            throw new IllegalStateException("File did not contain '" + FILE_TYPE + "' marker");
        }

        final var precisionBytes = input.readByte();
        final var indexBytes = input.readByte();

        final var vertexCount = input.readLong();
        final var faceCount = input.readLong();
        final var edgeCount = input.readLong();

        final var min = new Vertex(input.readDouble(), input.readDouble(), input.readDouble());
        final var max = new Vertex(input.readDouble(), input.readDouble(), input.readDouble());
        final var boundingBox = new BoundingBox(min, max);

        return new BffHeader(
                precisionBytes,
                indexBytes,
                vertexCount,
                faceCount,
                edgeCount,
                boundingBox
        );
    }

    public static Vertex readVertex(final DataInput input, final long bytesPerValue) throws IOException {
        return bytesPerValue == 4
                ? new Vertex(input.readFloat(), input.readFloat(), input.readFloat())
                : new Vertex(input.readDouble(), input.readDouble(), input.readDouble());
    }

    public static Face readFace(final DataInput input, final long bytesPerIndex) throws IOException {
        final var indices = bytesPerIndex == 4
                ? List.of((long) input.readInt(), (long) input.readInt(), (long) input.readInt())
                : List.of(input.readLong(), input.readLong(), input.readLong());
        return new Face(indices);
    }

    public static ResultBoundingBox readResultBoundingBox(final DataInput input) throws IOException {
        final var category = input.readInt();
        final var confidence = input.readDouble();
        final var boundingBox = readBoundingBox(input);
        return new ResultBoundingBox(category, confidence, boundingBox);
    }

    public static TriangleEvent readTriangleEvent(final DataInput input) throws IOException {
        final double z = input.readDouble();
        final var face = input.readLong();
        return new TriangleEvent(z, face);
    }

    private BinaryReader() {
        throw new UnsupportedOperationException("Utility class should not be instantiated.");
    }
}
