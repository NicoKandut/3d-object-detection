package nicok.bac.yolo3d.off;

import java.util.List;
import java.util.stream.Collectors;

public record IndexedMesh(List<Vertex> vertices, List<Face> faces) {

    public List<TriangleVertex> getTriangles() {
        return faces.stream()
                .flatMap(face -> face.toTriangles().stream())
                .map(index -> new TriangleVertex(
                        vertices.get((int) index.index1()),
                        vertices.get((int) index.index2()),
                        vertices.get((int) index.index3())
                ))
                .collect(Collectors.toList());

    }
}
