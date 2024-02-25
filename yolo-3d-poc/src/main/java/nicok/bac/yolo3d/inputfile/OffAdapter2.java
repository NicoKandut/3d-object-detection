package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.off.*;
import nicok.bac.yolo3d.off.VertexMesh.Line;

import java.util.ArrayList;
import java.util.Comparator;

public class OffAdapter2 implements InputFile {

    private static final String PREPROCESSED_PATH = "./pre";
    private final BoundingBox boundingBox;
    private final Volume3D volume;


    public OffAdapter2(final String path) throws Exception {
        final var boundingBoxBuilder = new BoundingBox.Builder();
        try (final var reader = new OffReader(path, 6)) {
            final var fileInfo = reader.readHeader();
            final var vertices = new ArrayList<Vertex>((int) fileInfo.vertexCount());
            final var faces = new ArrayList<Face>((int) fileInfo.faceCount());

            reader.readVertices(vertex -> {
                boundingBoxBuilder.withVertex(vertex);
                vertices.add(vertex);
            });
            reader.readFaces(faces::add);

            final var modelBounds = boundingBoxBuilder.build();

            final var indexedMesh = new IndexedMesh(vertices, faces);
            System.out.println("indexed mesh: " + indexedMesh.vertices().size() + " vertices, " + indexedMesh.faces().size() + " faces");
            final var triangles = indexedMesh.getTriangles();
            System.out.println("triangles: " + triangles.size());
            triangles.sort(Comparator.comparing(TriangleVertex::minZ));

            final var triangleMesh = new VertexMesh(triangles);
            var triangleEvents = triangleMesh.getEvents().stream()
                    .sorted(Comparator.comparing(VertexMesh.TriangleEvent::z))
                    .toList();

            System.out.println("Starting with " + triangleEvents.size() + " events");

            final var voxelSize = 0.05;
            final var voxelSizeHalf = voxelSize / 2;

            final var lines = new ArrayList<Line>();
            volume = new Volume3D(
                    (int) Math.ceil(modelBounds.size().x() / voxelSize) + 1,
                    (int) Math.ceil(modelBounds.size().y() / voxelSize) + 1,
                    (int) Math.ceil(modelBounds.size().z() / voxelSize) + 1
            );

            boundingBox = new BoundingBox(
                    Point.mul(1 / voxelSize, modelBounds.min()),
                    Point.mul(1 / voxelSize, modelBounds.max())
            );

            System.out.printf("Created volume (%d, %d, %d)\n",
                    (int) Math.ceil(modelBounds.size().x() / voxelSize),
                    (int) Math.ceil(modelBounds.size().y() / voxelSize),
                    (int) Math.ceil(modelBounds.size().z() / voxelSize)
            );

            var volZ = 0;
            for (var z = modelBounds.min().z(); z < modelBounds.max().z(); z += voxelSize) {
                // process events
                final var currentZ = z;
//                System.out.println("Current z: " + z);
                final var newEvents = triangleEvents.stream()
                        .takeWhile(event -> event.z() < currentZ)
                        .toList();
//                System.out.println("  Consuming " + newEvents.size() + " events. " + (triangleEvents.size() - newEvents.size()) + " left.");
                triangleEvents = triangleEvents.subList(newEvents.size(), triangleEvents.size());

                newEvents.forEach(event -> {
                    switch (event.type()) {
                        case BEGIN -> lines.add(event.line());
                        case MID -> event.line().isUpperHalf = true;
                        case END -> lines.remove(event.line());
                    }
                });

                final var linesAtZ = lines.stream()
                        .map(line -> line.atZ(currentZ))
                        .toList();

                // create outlines
                var volY = 0;
                for (var y = modelBounds.min().y(); y < modelBounds.max().y(); y += voxelSize) {
//                    System.out.println("--Current y: " + y);
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
                    for (var x = modelBounds.min().x(); x < modelBounds.max().x(); x += voxelSize) {
                        while (pointIndex < pointsAtY.size() && pointsAtY.get(pointIndex).x() < x) {
                            filled = !filled;
                            ++pointIndex;
                        }

                        if (filled) {
//                            System.out.printf("      Setting voxel at (%d, %d, %d)\n", volX, volY, volZ);
                            volume.set(volX, volY, volZ, 1, 1, 1);
                        }
                        ++volX;
                    }
                    ++volY;
                }
                ++volZ;
            }
        }
    }


    @Override
    public Volume3D read(final BoundingBox target) {
        System.out.printf("Reading %s\n", target);

        return volume;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }
}
