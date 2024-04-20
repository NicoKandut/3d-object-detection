package nicok.bac.yolo3d.voxelization;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.inputfile.TriangleEventIterator;
import nicok.bac.yolo3d.mesh.TriangleVertex;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.storage.FloatWrite3D;
import nicok.bac.yolo3d.storage.RandomAccessMeshReader;
import nicok.bac.yolo3d.storage.chunkstore.ChunkStore;
import nicok.bac.yolo3d.terminal.ProgressBar;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;

public class Voxelizer {

    public record PointX(double x, double normal) {
    }

    /**
     * Voxelizes a mesh into a 3D volume.
     * Use this version if your mesh (and the resulting volume) is small enough to fit into memory.
     */
    public static Volume3D voxelize(
            final TriangleEventIterator triangleEvents,
            final RandomAccessMeshReader reader,
            final double voxelSize,
            final BoundingBox target,
            final boolean print
    ) {
        final var boundingBox = new BoundingBox(
                Vertex.mul(target.min(), 1 / voxelSize),
                Vertex.mul(target.max(), 1 / voxelSize)
        );
        final var volume = Volume3D.forBoundingBox(boundingBox);
        voxelizeTo(volume, triangleEvents, reader, voxelSize, target, print);
        return volume;
    }

    /**
     * Voxelizes a mesh into a chunk store.
     * Use this version if your mesh (or the resulting volume) is too large to fit into memory.
     */
    public static ChunkStore saveChunkStore(
            final TriangleEventIterator triangleEvents,
            final RandomAccessMeshReader reader,
            final double voxelSize,
            final BoundingBox target,
            final String name,
            final boolean print
    ) throws IOException {
        final var chunkStore = ChunkStore.writer(name, target);
        voxelizeTo(chunkStore, triangleEvents, reader, voxelSize, target, print);
        return chunkStore;
    }

    private static void voxelizeTo(
            final FloatWrite3D target,
            final TriangleEventIterator triangleEvents,
            final RandomAccessMeshReader reader,
            final double voxelSize,
            final BoundingBox boundingBox,
            final boolean print
    ) {
        final var progress = new ProgressBar(20, (long) (boundingBox.size().z() / voxelSize));
        final var faces = new HashMap<Long, TriangleVertex>();

        // scan over mesh in z direction
        final var halfVoxel = voxelSize / 2.0;
        var volZ = 0;
        for (var z = boundingBox.min().z() + halfVoxel; z < boundingBox.max().z(); z += voxelSize) {
            // process events
            final var currentZ = z;
            final var newEvents = triangleEvents.takeWhile(event -> event.z() <= currentZ);

            newEvents.forEach(event -> {
                if (faces.containsKey(event.face())) {
                    faces.remove(event.face());
                } else {
                    final var indexFace = reader.getFace(event.face());
                    final var vertexFace = new TriangleVertex(
                            reader.getVertex(indexFace.vertexIndices().get(0)),
                            reader.getVertex(indexFace.vertexIndices().get(1)),
                            reader.getVertex(indexFace.vertexIndices().get(2))
                    );
                    faces.put(event.face(), vertexFace);
                }
            });

            // determine outlines of mesh at current z level
            final var linesAtZ = faces.values()
                    .stream()
                    .map(face -> face.zIntersect(currentZ))
                    .toList();

            var volY = 0;
            for (var y = boundingBox.min().y() + halfVoxel; y < boundingBox.max().y(); y += voxelSize) {
                final var currentY = y;
                final var pointsAtY = linesAtZ.stream()
                        .filter(line -> line.v1().y() <= currentY && line.v2().y() >= currentY ||
                                line.v2().y() <= currentY && line.v1().y() >= currentY)
                        .map(line -> line.zIntersect(currentY))
                        .sorted(Comparator.comparing(PointX::x))
                        .toList();

                var fillDepth = 0;
                var volX = 0;
                var pointIndex = 0;
                for (var x = boundingBox.min().x() + halfVoxel; x < boundingBox.max().x(); x += voxelSize) {
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

                        if (pointIndex == pointsAtY.size() - 1) {
                            fillDepth = 0;
                        }

                        pointIndex++;
                    }

                    if (fillDepth > 0 || override) {
                        target.set(volX, volY, volZ, 1f);
                    }
                    ++volX;
                }
                ++volY;
            }
            ++volZ;

            if (print) {
                progress.printProgress(volZ);
            }
        }

        if (print) {
            System.out.println("done");
        }
    }
}
