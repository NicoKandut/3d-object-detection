package nicok.bac.yolo3d.preprocessing;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.mesh.Vertex;

public class FitToBox implements Transformation {

    private BoundingBox sourceBoundingBox;
    private BoundingBox targetBoundingBox;
    private double scale = 1;
    private Vertex offset = new Vertex(0, 0, 0);

    @Override
    public Vertex apply(final Vertex vertex) {
        final var fromOrigin = Vertex.sub(vertex, this.sourceBoundingBox.min());
        final var scaled = Vertex.mul(fromOrigin, scale);
        final var fromTargetMin = Vertex.add(scaled, this.targetBoundingBox.min());
        return Vertex.add(fromTargetMin, this.offset);
    }

    public FitToBox withSourceBoundingBox(final BoundingBox sourceBoundingBox) {
        this.sourceBoundingBox = sourceBoundingBox;
        update();
        return this;
    }

    public FitToBox withTargetBoundingBox(final BoundingBox targetBoundingBox) {
        this.targetBoundingBox = targetBoundingBox;
        update();
        return this;
    }

    private void update() {
        recalculateScale();
        recalculateOffset();
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

        this.offset = Vertex.div(Vertex.sub(targetBoundingBox.size(), Vertex.mul(sourceBoundingBox.size(), scale)), 2);
    }
}
