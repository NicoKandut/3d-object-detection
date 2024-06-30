package nicok.bac.yolo3d.mesh;

import java.util.List;

public record Face(
        List<Long> vertexIndices
) {

    public static Face from(final TriangleIndex index) {
        return new Face(List.of(
                index.index1(),
                index.index2(),
                index.index3()
        ));
    }
}
