package nicok.bac.yolo3d.boundingbox;

import nicok.bac.yolo3d.mesh.Vertex;

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
        if (min.x() > max.x() || min.y() > max.y() || min.z() > max.z()) {
            throw new IllegalArgumentException("min must be less than or equal to max");
        }
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

    public static BoundingBox withOffset(BoundingBox box, Vertex offset) {
        return new BoundingBox(
                Vertex.add(box.min, offset),
                Vertex.add(box.max, offset)
        );
    }

    /**
     * Returns the center of the bounding box.
     */
    public Vertex center() {
        return Vertex.div(Vertex.add(min, max), 2);
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

    public boolean contains(final Vertex vertex) {
        return vertex.x() >= min.x() &&
                vertex.x() < max.x() &&
                vertex.y() >= min.y() &&
                vertex.y() < max.y() &&
                vertex.z() >= min.z() &&
                vertex.z() < max.z();
    }

    public boolean contains(final BoundingBox other) {
        return this.min().x() <= other.min().x() &&
                this.min().y() <= other.min().y() &&
                this.min().z() <= other.min().z() &&
                this.max().x() >= other.max().x() &&
                this.max().y() >= other.max().y() &&
                this.max().z() >= other.max().z();
    }

    @Override
    public String toString() {
        return String.format("[%s to %s]", min, max);
    }

    /**
     * A builder for creating bounding boxes.
     * Can be used as a "mutable" bounding box.
     */
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

        public void withVertices(final Vertex... vertices) {
            for (final var vertex : vertices) {
                withVertex(vertex);
            }
        }

        public void reset() {
            minX = Double.MAX_VALUE;
            minY = Double.MAX_VALUE;
            minZ = Double.MAX_VALUE;
            maxX = Double.MIN_VALUE;
            maxY = Double.MIN_VALUE;
            maxZ = Double.MIN_VALUE;
        }

        public BoundingBox build() {
            return new BoundingBox(
                    new Vertex(minX, minY, minZ),
                    new Vertex(maxX, maxY, maxZ)
            );
        }

        public boolean contains(final Vertex vertex) {
            return vertex.x() >= minX &&
                    vertex.x() < maxX &&
                    vertex.y() >= minY &&
                    vertex.y() < maxY &&
                    vertex.z() >= minZ &&
                    vertex.z() < maxZ;
        }
    }
}
