package nicok.bac.yolo3d;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.ResultBoundingBox;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;

public record BoxFilter(double threshold) {

    public List<ResultBoundingBox> filter(List<ResultBoundingBox> boxes) {

        // sort boxes by confidence
        final var qualifiedBoxes = boxes.stream()
                .sorted(comparing(ResultBoundingBox::confidence).reversed())
                .filter(box -> box.confidence() >= threshold)
                .toList();

        // exclude boxes with high intersect over union
        final var result = new ArrayList<ResultBoundingBox>();
        for (final var box : qualifiedBoxes) {
            var isNew = true;
            for (final var addedBox : result) {
                if (box.category() == addedBox.category() && BoundingBox.getIntersectOverUnion(box.boundingBox(), addedBox.boundingBox()) > 0.5) {
                    isNew = false;
                }
            }
            if(isNew) {
                result.add(box);
            }
        }

        return result;
    }
}
