package nicok.bac.yolo3d.preprocessing;

import com.scs.voxlib.Voxel;
import nicok.bac.yolo3d.mesh.Vertex;

/**
 * A transformation that can be applied to a vertex or a voxel.
 * To implement new Transformations, create a new class that implements this interface.
 *
 * @see nicok.bac.yolo3d.preprocessing.LinearTransformation
 * @see nicok.bac.yolo3d.preprocessing.FitToBox
 */
public interface Transformation {

    /**
     * Apply the transformation to a vertex.
     */
    Vertex apply(final Vertex vertex);

    /**
     * Default code to apply Vertex transformation to a Voxel.
     * This might not be efficient enough in some cases.
     */
    default Voxel apply(final Voxel voxel) {
        final var vertex = new Vertex(
                voxel.getPosition().x,
                voxel.getPosition().y,
                voxel.getPosition().z
        );
        final var transformed = this.apply(vertex);
        return new Voxel(
                (int) transformed.x(),
                (int) transformed.y(),
                (int) transformed.z(),
                voxel.getColourIndexByte()
        );
    }
}
