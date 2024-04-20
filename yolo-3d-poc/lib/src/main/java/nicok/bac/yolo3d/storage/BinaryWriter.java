package nicok.bac.yolo3d.storage;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.TriangleEvent;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.storage.bff.BffFormat;
import nicok.bac.yolo3d.storage.bff.BffHeader;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Writes binary data to a {@link DataOutput}.
 */
public final class BinaryWriter {

    private BinaryWriter() {
        throw new UnsupportedOperationException("Utility class should not be instantiated.");
    }

    public static void write(final DataOutput output, final BoundingBox boundingBox) throws IOException {
        output.writeDouble(boundingBox.min().x());
        output.writeDouble(boundingBox.min().y());
        output.writeDouble(boundingBox.min().z());
        output.writeDouble(boundingBox.max().x());
        output.writeDouble(boundingBox.max().y());
        output.writeDouble(boundingBox.max().z());
    }

    public static void write(final DataOutput output, final BffHeader header) throws IOException {
        output.write(BffFormat.FILE_TYPE.getBytes());
        output.writeByte((byte) header.precisionBytes());
        output.writeByte((byte) header.indexBytes());
        output.writeLong(header.vertexCount());
        output.writeLong(header.faceCount());
        output.writeLong(header.edgeCount());
        BinaryWriter.write(output, header.boundingBox());
    }

    public static void write(final DataOutput output, final Vertex vertex, final long bytesPerValue) throws IOException {
        if (bytesPerValue == 4) {
            output.writeFloat((float) vertex.x());
            output.writeFloat((float) vertex.y());
            output.writeFloat((float) vertex.z());
        } else {
            output.writeDouble(vertex.x());
            output.writeDouble(vertex.y());
            output.writeDouble(vertex.z());
        }
    }

    public static void write(final DataOutput output, final Face face, final long bytesPerIndex) throws IOException {
        if (bytesPerIndex == 4) {
            for (final var index : face.vertexIndices()) {
                output.writeInt(index.intValue());
            }
        } else {
            for (final var index : face.vertexIndices()) {
                output.writeLong(index);
            }
        }
    }

    public static void write(final DataOutput output, final ResultBoundingBox resultBoundingBox) throws IOException {
        output.writeInt(resultBoundingBox.category());
        output.writeDouble(resultBoundingBox.confidence());
        BinaryWriter.write(output, resultBoundingBox.boundingBox());
    }

    public static void write(final DataOutput output, final TriangleEvent value) throws IOException {
        output.writeDouble(value.z());
        output.writeLong(value.face());
    }
}
