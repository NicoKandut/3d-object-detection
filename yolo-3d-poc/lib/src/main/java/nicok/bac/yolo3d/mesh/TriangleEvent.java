package nicok.bac.yolo3d.mesh;

import nicok.bac.yolo3d.collection.BinaryStreamAppend;

import java.io.DataOutputStream;
import java.io.IOException;

public record TriangleEvent(double z, Long face) implements BinaryStreamAppend {
    @Override
    public void appendTo(final DataOutputStream target) {
        try {
            target.writeDouble(z());
            target.writeLong(face());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
