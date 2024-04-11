package nicok.bac.yolo3d.preprocessing;

import nicok.bac.yolo3d.mesh.Vertex;

public class IdentityTransformation implements Transformation {
    @Override
    public Vertex apply(final Vertex vertex) {
        return vertex;
    }
}
