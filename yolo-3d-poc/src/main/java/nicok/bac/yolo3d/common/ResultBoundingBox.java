package nicok.bac.yolo3d.common;

public record ResultBoundingBox(
        Category category,
        double confidence,
        BoundingBox boundingBox
) {
}
