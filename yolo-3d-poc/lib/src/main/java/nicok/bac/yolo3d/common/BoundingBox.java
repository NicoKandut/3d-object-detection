package nicok.bac.yolo3d.common;

import com.scs.voxlib.GridPoint3;
import com.scs.voxlib.Voxel;
import nicok.bac.yolo3d.off.Vertex;

import static java.util.Objects.requireNonNull;

/**
 * Represents an axis-aligned bounding box in 3D space.
 */
public record BoundingBox(
        Vertex min,
        Vertex max
) {
    public BoundingBox {
        requireNonNull(min);
        requireNonNull(max);
    }

    /**
     * Creates a new bounding box from the origin to the given vertex.
     */
    public static BoundingBox fromOrigin(final Vertex to) {
        return new BoundingBox(Vertex.ORIGIN, to);
    }

    /**
     * Creates a new bounding box from the origin to the point (size, size, size).
     */
    public static BoundingBox fromOrigin(final double size) {
        return new BoundingBox(Vertex.ORIGIN, new Vertex(size, size, size));
    }

    /**
     * Returns the size of the bounding box.
     */
    public Vertex size() {
        return Vertex.sub(max, min);
    }

    /**
     * Calculates both the intersection and the union of two bounding boxes and returns the intersection over union.
     */
    public static double getIntersectOverUnion(final BoundingBox a, final BoundingBox b) {
        final var intersectMin = Vertex.max(a.min, b.min);
        final var intersectMax = Vertex.min(a.max, b.max);
        final var intersectWhd = Vertex.max(Vertex.sub(intersectMax, intersectMin), Vertex.ORIGIN);
        final var intersectVolume = intersectWhd.x() * intersectWhd.y() * intersectWhd.z();

        final var aWhd = Vertex.sub(a.max, a.min);
        final var bWhd = Vertex.sub(b.max, b.min);
        final var predVolume = aWhd.x() * aWhd.y() * aWhd.z();
        final var trueVolume = bWhd.x() * bWhd.y() * bWhd.z();

        final var unionVolume = predVolume + trueVolume - intersectVolume;

        return intersectVolume / unionVolume;
    }

    public static BoundingBox addOffset(BoundingBox box, Vertex offset) {
        return new BoundingBox(
                Vertex.add(box.min, offset),
                Vertex.add(box.max, offset)
        );
    }

    public boolean contains(final GridPoint3 point) {
        return point.x >= min.x() && point.x < max.x() &&
                point.y >= min.y() && point.y < max.y() &&
                point.z >= min.z() && point.z < max.z();
    }

    public boolean contains(final Vertex vertex) {
        return vertex.x() >= min.x() && vertex.x() < max.x() &&
                vertex.y() >= min.y() && vertex.y() < max.y() &&
                vertex.z() >= min.z() && vertex.z() < max.z();
    }

    public boolean contains(final BoundingBox other) {
        return this.min().x() <= other.min().x() &&
                this.min().y() <= other.min().y() &&
                this.min().z() <= other.min().z() &&
                this.max().x() >= other.max().x() &&
                this.max().x() >= other.max().y() &&
                this.max().x() >= other.max().z();
    }

    /**
     * Returns the center of the bounding box.
     */
    public Vertex center() {
        return new Vertex(
                (min.x() + max.x()) / 2.0,
                (min.y() + max.y()) / 2.0,
                (min.z() + max.z()) / 2.0
        );
    }

    @Override
    public String toString() {
        return String.format("[%s to %s]", min, max);
    }

    public static final class Builder {
        private double minX = Double.MAX_VALUE;
        private double minY = Double.MAX_VALUE;
        private double minZ = Double.MAX_VALUE;
        private double maxX = Double.MIN_VALUE;
        private double maxY = Double.MIN_VALUE;
        private double maxZ = Double.MIN_VALUE;

        public void withVertex(final Vertex vertex) {
            minX = Double.min(minX, vertex.x());
            minY = Double.min(minY, vertex.y());
            minZ = Double.min(minZ, vertex.z());
            maxX = Double.max(maxX, vertex.x());
            maxY = Double.max(maxY, vertex.y());
            maxZ = Double.max(maxZ, vertex.z());
        }

        public void withVoxel(final Voxel voxel) {
            minX = Double.min(minX, voxel.getPosition().x);
            minY = Double.min(minY, voxel.getPosition().y);
            minZ = Double.min(minZ, voxel.getPosition().z);
            maxX = Double.max(maxX, voxel.getPosition().x);
            maxY = Double.max(maxY, voxel.getPosition().y);
            maxZ = Double.max(maxZ, voxel.getPosition().z);
        }

        public BoundingBox build() {
            return new BoundingBox(
                    new Vertex(minX, minY, minZ),
                    new Vertex(maxX, maxY, maxZ)
            );
        }
    }
}
