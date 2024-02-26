package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.off.Face;
import nicok.bac.yolo3d.off.OffReader;
import nicok.bac.yolo3d.off.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OffAdapter implements InputFile {

    private static final String PREPROCESSED_PATH = "./pre";

    private final List<Vertex> vertices;
    private final List<Face> faces;
    private final BoundingBox boundingBox;

    public OffAdapter(final String path) throws Exception {
        final var boundingBoxBuilder = new BoundingBox.Builder();
        try (final var reader = new OffReader(path)) {
            final var fileInfo = reader.readHeader();

            if (fileInfo.vertexCount() > 1000) {
                vertices = Collections.emptyList();
                faces = Collections.emptyList();
                reader.readVertices(boundingBoxBuilder::withVertex);
                boundingBox = boundingBoxBuilder.build();
            } else {
                vertices = new ArrayList<>((int) fileInfo.vertexCount());
                faces = new ArrayList<>((int) fileInfo.faceCount());

                reader.readVertices(vertex -> {
                    boundingBoxBuilder.withVertex(vertex);
                    vertices.add(vertex);
                });
                reader.readFaces(faces::add);

                boundingBox = boundingBoxBuilder.build();
            }

            System.out.println("Fully processed .off file");
            System.out.printf("  - %d vertices\n", vertices.size());
            System.out.printf("  - %d faces\n", faces.size());
            System.out.printf("  - extends %s\n", boundingBox);
        }
    }

    @Override
    public Volume3D read(final BoundingBox target) {
        System.out.printf("Reading %s\n", target);

        final var targetVertices = new ArrayList<Vertex>();
        if (!vertices.isEmpty()) {
            vertices.stream()
                    .filter(target::contains)
                    .forEach(targetVertices::add);
        }

        final var targetFaces = new ArrayList<Face>();
        if (!faces.isEmpty()) {
            faces.stream()
                    .filter(face -> face.vertexIndices().stream()
                            .map(vertices::get)
                            .anyMatch(target::contains)
                    )
                    .forEach(targetFaces::add);
        }

        final var volume = new Volume3D(
                (int) target.size().x(),
                (int) target.size().y(),
                (int) target.size().z()
        );

//        if (targetFaces.isEmpty()) {
//            System.out.println("Empty chunk detected");
//            return volume;
//        } else {
//            System.out.printf("%d relevant faces", targetFaces.size());
//        }

        List<Point> pointsInside = new ArrayList<>(
                (int) target.size().x() *
                        (int) target.size().y() *
                        (int) target.size().z()
        );

        for (var i = 0; i < (int) target.size().x(); ++i) {
            for (var j = 0; j < (int) target.size().y(); ++j) {
                for (var k = 0; k < (int) target.size().z(); ++k) {
                    Face nearestFace = null;
                    var nearestDistance = Double.MAX_VALUE;
                    final var point = new Point(i, j, k);
//                    System.out.println("Searching nearest face");
                    for (final var face : faces) {
                        final var normal = face.normal(vertices).normalize();

                        double dist = Point.dot(point, normal) - Point.dot(face.center(vertices), normal);
                        final var projected = Point.sub(point, Point.mul(dist, normal));


//                        if (distance < nearestDistance) {
////                            System.out.println(" - new distance: " + distance);
//                            nearestDistance = distance;
//                            nearestFace = face;
//                        }
                    }

                    assert (nearestFace != null);

                    final var normalVector = nearestFace.normal(vertices);
                    final var distance = point.x() * normalVector.x() + point.y() * normalVector.y() + point.z() * normalVector.z();
                    if (distance < 0) {
                        pointsInside.add(point);
                    }
                }
            }
        }

//        System.out.printf("Starting with %d points\n", pointsInside.size());

//        for (final var face : targetFaces) {
//            final var normalVector = face.normalVector(vertices);
//
//            pointsInside = pointsInside.stream()
//                    .filter(point -> point.x() * normalVector.x() + point.y() * normalVector.y() + point.z() * normalVector.z() < 1.0)
//                    .toList();
//
//            System.out.printf("  - %d points left\n", pointsInside.size());
//        }

        System.out.printf("  - %d points inside\n", pointsInside.size());

        pointsInside.forEach(point -> volume.set((int) point.x(), (int) point.y(), (int) point.z(), 1, 1, 1));

        return volume;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }
}
