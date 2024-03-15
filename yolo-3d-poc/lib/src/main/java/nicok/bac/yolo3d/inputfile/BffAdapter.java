package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.bff.BffReader;
import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.mesh.MeshConverter;
import nicok.bac.yolo3d.off.OffMesh;
import nicok.bac.yolo3d.off.OffReader;
import nicok.bac.yolo3d.off.VertexMesh;
import nicok.bac.yolo3d.preprocessing.Transformation;
import nicok.bac.yolo3d.voxelization.Voxelizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BffAdapter implements InputFile {

    private OffMesh mesh;
    private List<VertexMesh.TriangleEvent> triangleEvents;

    public BffAdapter(final String path) throws Exception {
        try (final var reader = new BffReader(path)) {
            final var header = reader.readHeader();
            final var boundingBox = new BoundingBox.Builder();
            final var vertices = IntStream.range(0, header.vertexCount())
                    .mapToObj(reader::readVertex)
                    .peek(boundingBox::withVertex)
                    .toList();
            final var faces = IntStream.range(0, header.faceCount())
                    .mapToObj(reader::readFace)
                    .toList();

            this.mesh = new OffMesh(
                    vertices,
                    faces,
                    boundingBox.build()
            );

            triangleEvents = getTriangleEvents();
        }
    }

    private List<VertexMesh.TriangleEvent> getTriangleEvents() {
        final var triangleMesh = MeshConverter.getTriangleMesh(mesh);
//            System.out.println("triangles: " + triangleMesh.triangles().size());
        return triangleMesh.getEvents().stream()
                .sorted(Comparator.comparing(VertexMesh.TriangleEvent::z))
                .toList();
    }

    @Override
    public Volume3D read(final BoundingBox target) {
        final var voxelSize = 1.0;
        return Voxelizer.voxelize(triangleEvents, voxelSize, target);
    }

    @Override
    public InputFile withPreprocessing(final Transformation preProcessing) {
        final var boundingBox = new BoundingBox.Builder();

        mesh = new OffMesh(
                mesh.vertices().stream()
                        .map(preProcessing::apply)
                        .peek(boundingBox::withVertex)
                        .toList(),
                mesh.faces(),
                boundingBox.build()
        );

        triangleEvents = getTriangleEvents();

        return this;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return mesh.boundingBox();
    }
}
