package nicok.bac.yolo3d.preprocessing;

import com.scs.voxlib.Voxel;
import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.off.Vertex;

public interface Transformation {
    Vertex apply(final Vertex vertex);

    default Point apply(final Point point) {
        final var vertex = this.apply(new Vertex(point.x(), point.y(), point.z()));
        return new Point(vertex.x(), vertex.y(), vertex.z());
    }

    default Voxel apply(final Voxel voxel) {
        final var transformed = this.apply(new Vertex(
                voxel.getPosition().x,
                voxel.getPosition().y,
                voxel.getPosition().z
        ));
        return new Voxel(
                (int) transformed.x(),
                (int) transformed.y(),
                (int) transformed.z(),
                voxel.getColourIndexByte()
        );
    }

    default BoundingBox apply(final BoundingBox boundingBox) {
        return new BoundingBox(
                this.apply(boundingBox.min()),
                this.apply(boundingBox.max())
        );
    }
}
