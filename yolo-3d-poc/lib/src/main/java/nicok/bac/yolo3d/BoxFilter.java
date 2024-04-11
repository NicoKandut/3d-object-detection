package nicok.bac.yolo3d;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.collection.PersistentResultBoundingBoxList;
import nicok.bac.yolo3d.common.ResultBoundingBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

/**
 * Filter boxes by confidence and intersect over union
 * Intended to be used to reduce the number of results,
 * after a file has been scanned by an object detection model.
 */
public record BoxFilter(
        double confidenceThreshold,
        double iouThreshold
) {
    public List<ResultBoundingBox> filter(final List<ResultBoundingBox> boxes) {
        return filter(boxes.stream()
                .sorted(comparing(ResultBoundingBox::confidence).reversed()));
    }

    public List<ResultBoundingBox> filter(final PersistentResultBoundingBoxList boxes) throws IOException {
        final var sorted = boxes.sort(Comparator.comparing(ResultBoundingBox::confidence).reversed());
        try (final var stream = sorted.stream()) {
            return filter(stream);
        }
    }

    public List<ResultBoundingBox> filter(final Stream<ResultBoundingBox> boxes) {

        // sort boxes by confidence
        final var qualifiedBoxes = boxes
                .filter(box -> box.confidence() >= confidenceThreshold)
                .toList();

        // exclude boxes that have high overlap with other boxes that have higher confidence
        final var result = new ArrayList<ResultBoundingBox>();
        for (final var box : qualifiedBoxes) {
            var isNew = true;
            for (final var addedBox : result) {
                if (box.category() == addedBox.category() && BoundingBox.getIntersectOverUnion(box.boundingBox(), addedBox.boundingBox()) > iouThreshold) {
                    isNew = false;
                    break;
                }
            }
            if (isNew) {
                result.add(box);
            }
        }

        return result;
    }
}
