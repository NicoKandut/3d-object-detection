package nicok.bac.yolo3d.util;

import nicok.bac.yolo3d.off.Face;

import java.util.ArrayList;
import java.util.List;

public final class Triangulation {

    private Triangulation() {
    }

    public static List<Face> shell(final Face face) {
        final var indices = face.vertexIndices();
        final var i0 = 0;
        final var faces = new ArrayList<Face>(indices.size() - 2);
        for (var i1 = 1; i1 < indices.size() - 1; ++i1) {
            final var i2 = i1 + 1;
            faces.add(new Face(List.of(
                    indices.get(i0),
                    indices.get(i1),
                    indices.get(i2)
            )));
        }
        return faces;
    }
}
