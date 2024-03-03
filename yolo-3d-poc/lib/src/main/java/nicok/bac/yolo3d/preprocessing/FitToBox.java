package nicok.bac.yolo3d.preprocessing;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.off.Vertex;

public class FitToBox implements Transformation {

    private BoundingBox sourceBoundingBox;
    private BoundingBox targetBoundingBox;
    private double scale = 1;

    @Override
    public Vertex apply(final Vertex vertex) {
        return Vertex.mul(scale, vertex);
    }

    public FitToBox withSourceBoundingBox(final BoundingBox sourceBoundingBox) {
        this.sourceBoundingBox = sourceBoundingBox;
        recalculateScale();
        return this;
    }

    public FitToBox withTargetBoundingBox(final BoundingBox targetBoundingBox) {
        this.targetBoundingBox = targetBoundingBox;
        recalculateScale();
        return this;
    }

    private void recalculateScale() {
        if (sourceBoundingBox == null || targetBoundingBox == null) {
            return;
        }

        final var scaleX = targetBoundingBox.size().x() / sourceBoundingBox.size().x();
        final var scaleY = targetBoundingBox.size().y() / sourceBoundingBox.size().y();
        final var scaleZ = targetBoundingBox.size().z() / sourceBoundingBox.size().z();

        this.scale = Math.min(Math.min(scaleX, scaleY), scaleZ);
    }
}
