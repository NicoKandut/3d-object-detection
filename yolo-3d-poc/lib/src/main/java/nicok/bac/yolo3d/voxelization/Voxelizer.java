package nicok.bac.yolo3d.voxelization;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.off.Vertex;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.off.VertexMesh;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Voxelizer {

    public record PointX(double x, double normal) {
    }

    public static Volume3D voxelize(
            List<VertexMesh.TriangleEvent> triangleEvents,
            final double voxelSize,
            BoundingBox target
    ) {
        final var boundingBox = new BoundingBox(
                Vertex.mul(target.min(), 1 / voxelSize),
                Vertex.mul(target.max(), 1 / voxelSize)
        );

        final var lines = new ArrayList<VertexMesh.Line>();
        final var volume = Volume3D.forBoundingBox(boundingBox);

        // scan over mesh in z direction
        final var halfVoxel = voxelSize / 2.0;
        var volZ = 0;
        for (var z = target.min().z() + halfVoxel; z < target.max().z(); z += voxelSize) {
            // process events
            final var currentZ = z;
            final var newEvents = triangleEvents.stream()
                    .takeWhile(event -> event.z() <= currentZ)
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
            for (var y = target.min().y() + halfVoxel; y < target.max().y(); y += voxelSize) {
                final var currentY = y;
                final var pointsAtY = linesAtZ.stream()
                        .filter(line -> line.v1().y() <= currentY && line.v2().y() >= currentY ||
                                line.v2().y() <= currentY && line.v1().y() >= currentY)
                        .map(line -> line.atY(currentY))
                        .sorted(Comparator.comparing(PointX::x))
                        .toList();

//                if (pointsAtY.size() % 2 == 1) {
//                    System.out.println("Warning: uneven number of points will cause artifacts. y=" + volY + ", z=" + volZ);
//                }

                var fillDepth = 0;
                var volX = 0;
                var pointIndex = 0;
                for (var x = target.min().x() + halfVoxel; x < target.max().x(); x += voxelSize) {
                    var override = false;
                    while (pointIndex < pointsAtY.size() && pointsAtY.get(pointIndex).x() < x) {
                        override = true;
                        final var normal = pointsAtY.get(pointIndex).normal();
                        if (normal < 0) {
                            fillDepth += 1;
                        }

                        if (normal > 0) {
                            fillDepth -= 1;
                        }

                        pointIndex++;
                    }

                    // filled for large bodies, pointsInPixel for more solid surfaces
                    if (fillDepth > 0 || override) {
                        volume.set(volX, volY, volZ, 1f);
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
