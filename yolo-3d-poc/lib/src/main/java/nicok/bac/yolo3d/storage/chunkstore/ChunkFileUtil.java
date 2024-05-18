package nicok.bac.yolo3d.storage.chunkstore;

import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.util.DirectoryUtil;

public final class ChunkFileUtil {

    public static String getChunkPath(final String directory, final Vertex chunkPosition) {
        return "%s/%d_%d_%d.bin".formatted(directory, (int) chunkPosition.x(), (int) chunkPosition.y(), (int) chunkPosition.z());
    }

    public static Vertex getChunkPosition(final String chunkPath, final long chunkSize) {
        final var name = DirectoryUtil.getFilename(chunkPath);
        final var xyz = name.split("_");
        final var x = Long.parseLong(xyz[0]);
        final var y = Long.parseLong(xyz[1]);
        final var z = Long.parseLong(xyz[2]);
        return Vertex.mul(new Vertex(x, y, z), chunkSize);
    }

    public static Vertex relativePositionFromIndex(final long i, final long chunkSize) {
        return new Vertex(
                (double) (i % chunkSize),
                (double) ((i / chunkSize) % chunkSize),
                (double) (i / (chunkSize * chunkSize))
        );
    }

    public static long indexFromRelativePosition(final Vertex relativePosition, final long chunkSize) {
        return (long) relativePosition.x()
                + (long) relativePosition.y() * chunkSize
                + (long) relativePosition.z() * chunkSize * chunkSize;
    }
}
