package nicok.bac.yolo3d.preprocessing;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.off.Vertex;

public class FitToBox implements Transformation {

    private BoundingBox sourceBoundingBox;
    private BoundingBox targetBoundingBox;
    private double scale = 1;
    private Vertex offset = new Vertex(0, 0, 0);

    @Override
    public Vertex apply(final Vertex vertex) {
        return Vertex.add(Vertex.mul(scale, vertex), offset);
    }

    public FitToBox withSourceBoundingBox(final BoundingBox sourceBoundingBox) {
        this.sourceBoundingBox = sourceBoundingBox;
        recalculateScale();
        recalculateOffset();
        return this;
    }

    public FitToBox withTargetBoundingBox(final BoundingBox targetBoundingBox) {
        this.targetBoundingBox = targetBoundingBox;
        recalculateScale();
        recalculateOffset();
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

    private void recalculateOffset() {
        if (sourceBoundingBox == null || targetBoundingBox == null) {
            return;
        }

        final var offsetX = (targetBoundingBox.min().x() - sourceBoundingBox.min().x() * scale) + (targetBoundingBox.size().x() - sourceBoundingBox.size().x() * scale) / 2;
        final var offsetY = (targetBoundingBox.min().y() - sourceBoundingBox.min().y() * scale) + (targetBoundingBox.size().y() - sourceBoundingBox.size().y() * scale) / 2;
        final var offsetZ = (targetBoundingBox.min().z() - sourceBoundingBox.min().z() * scale) + (targetBoundingBox.size().z() - sourceBoundingBox.size().z() * scale) / 2;

        this.offset = new Vertex(offsetX, offsetY, offsetZ);
    }
}
