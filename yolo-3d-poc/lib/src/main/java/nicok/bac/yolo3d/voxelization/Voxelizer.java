package nicok.bac.yolo3d.voxelization;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.off.Vertex;
import nicok.bac.yolo3d.off.VertexMesh;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Voxelizer {

    public static Volume3D voxelize(
            List<VertexMesh.TriangleEvent> triangleEvents,
            final double voxelSize,
            BoundingBox target
    ) {
        final var boundingBox = new BoundingBox(
                Point.mul(1 / voxelSize, target.min()),
                Point.mul(1 / voxelSize, target.max())
        );

//        final var voxelSizeHalf = voxelSize / 2; // TODO: sample at center of voxel?

        final var lines = new ArrayList<VertexMesh.Line>();
        final var volume = Volume3D.forBoundingBox(boundingBox);

        // scan over mesh in z direction
        var volZ = 0;
        for (var z = target.min().z(); z < target.max().z(); z += voxelSize) {
            // process events
            final var currentZ = z;
            final var newEvents = triangleEvents.stream()
                    .takeWhile(event -> event.z() < currentZ)
                    .toList();
            triangleEvents = triangleEvents.subList(newEvents.size(), triangleEvents.size());

            newEvents.forEach(event -> {
                switch (event.type()) {
                    case BEGIN -> lines.add(event.line());
                    case MID -> event.line().isUpperHalf = true;
                    case END -> lines.remove(event.line());
                }
            });

            // determine outlines of mesh at current z level
            final var linesAtZ = lines.stream()
                    .map(line -> line.atZ(currentZ))
                    .toList();

            var volY = 0;
            for (var y = target.min().y(); y < target.max().y(); y += voxelSize) {
                final var currentY = y;
                final var pointsAtY = linesAtZ.stream()
                        .filter(line -> line.v1().y() <= currentY && line.v2().y() > currentY ||
                                line.v2().y() <= currentY && line.v1().y() > currentY)
                        .map(line -> line.atY(currentY))
                        .sorted(Comparator.comparing(Vertex::x))
                        .toList();
                var filled = false;
                var volX = 0;
                var pointIndex = 0;
                for (var x = target.min().x(); x < target.max().x(); x += voxelSize) {
                    while (pointIndex < pointsAtY.size() && pointsAtY.get(pointIndex).x() < x) {
                        filled = !filled;
                        ++pointIndex;
                    }

                    if (filled) {
                        volume.set(volX, volY, volZ, 1, 1, 1);
                    }
                    ++volX;
                }
                ++volY;
            }
            ++volZ;
        }

        return volume;
    }
}
