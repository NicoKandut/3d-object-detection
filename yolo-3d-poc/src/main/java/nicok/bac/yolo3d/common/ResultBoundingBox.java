package nicok.bac.yolo3d.common;

public record ResultBoundingBox(
        Category category,
        double confidence,
        BoundingBox boundingBox
) {
    public static ResultBoundingBox fromOutput(
            final CellOutput output,
            final BoundingBox frame,
            final int i,
            final int j,
            final int k
    ) {
        final var bestCategory = getBestCategory(output.classConfidence());
        final var scaledOutput = from_cell_repr(
                i, j, k,
                output,
                7,
                28
        );
        final var relativeBox = xyzwhd_to_minmax(
                scaledOutput.x(), scaledOutput.y(), scaledOutput.z(),
                scaledOutput.w(), scaledOutput.h(), scaledOutput.d()
        );
        final var absoluteBox = BoundingBox.addOffset(relativeBox, frame.min());

        return new ResultBoundingBox(
                bestCategory,
                output.confidence(),
                absoluteBox
        );
    }


    private static Category getBestCategory(float[] class_confidence) {
        var max_value = -1.0f;
        var max_index = -1;

        for (var index = 0; index < class_confidence.length; index++) {
            if (class_confidence[index] >= max_value) {
                max_value = class_confidence[index];
                max_index = index;
            }

        }

        assert (max_index >= 0 && max_index < Category.values().length);

        return Category.values()[max_index];
    }

    private static CellOutput from_cell_repr(
            int i, int j, int k,
            CellOutput output,
            int cell_count,
            int full_size
    ) {
        return new CellOutput(
                output.classConfidence(),
                (output.x() + i) * full_size / cell_count,
                (output.y() + j) * full_size / cell_count,
                (output.z() + k) * full_size / cell_count,
                output.w() * full_size,
                output.h() * full_size,
                output.d() * full_size,
                output.confidence()
        );
    }

    private static BoundingBox xyzwhd_to_minmax(float x, float y, float z, float w, float h, float d) {
        final var minX = x - w / 2;
        final var minY = y - h / 2;
        final var minZ = z - d / 2;
        final var maxX = x + w / 2;
        final var maxY = y + h / 2;
        final var maxZ = z + d / 2;

        return new BoundingBox(
                new Point(minX, minY, minZ),
                new Point(maxX, maxY, maxZ)
        );
    }

    @Override
    public String toString() {
        return String.format("%f %s %s", confidence, category, boundingBox);
    }
}
