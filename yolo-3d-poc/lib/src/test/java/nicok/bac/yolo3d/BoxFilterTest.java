package nicok.bac.yolo3d;

import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.mesh.Vertex;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nicok.bac.yolo3d.boundingbox.BoundingBox.fromOrigin;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BoxFilterTest {

    @Test
    void itFiltersByConfidenceAndIouForSingleCategory() {
        final var boxes = List.of(
                new ResultBoundingBox(0, 1.0, fromOrigin(new Vertex(1, 1, 1))),
                new ResultBoundingBox(0, 0.9, fromOrigin(new Vertex(1, 1, 2))),
                new ResultBoundingBox(0, 0.8, fromOrigin(new Vertex(1, 1, 2))),
                new ResultBoundingBox(0, 0.7, fromOrigin(new Vertex(1, 1, 2))),
                new ResultBoundingBox(0, 0.6, fromOrigin(new Vertex(1, 1, 3))),
                new ResultBoundingBox(0, 0.5, fromOrigin(new Vertex(1, 1, 3))),
                new ResultBoundingBox(0, 0.4, fromOrigin(new Vertex(1, 1, 3))),
                new ResultBoundingBox(0, 0.3, fromOrigin(new Vertex(1, 1, 4))),
                new ResultBoundingBox(0, 0.2, fromOrigin(new Vertex(1, 1, 4))),
                new ResultBoundingBox(0, 0.1, fromOrigin(new Vertex(1, 1, 4)))
        );

        var result = new BoxFilter(0.0, 1.0).filter(boxes);
        assertEquals(boxes, result);

        result = new BoxFilter(0.5, 1.0).filter(boxes);
        assertEquals(boxes.subList(0, 6), result);

        result = new BoxFilter(0.0, 0.5).filter(boxes);
        assertEquals(List.of(boxes.get(0), boxes.get(1), boxes.get(7)), result);

        result = new BoxFilter(0.5, 0.5).filter(boxes);
        assertEquals(List.of(boxes.get(0), boxes.get(1)), result);
    }


    @Test
    void itHandlesTwoDifferentCategories() {
        final var boxes = List.of(
                new ResultBoundingBox(0, 1.00, fromOrigin(new Vertex(1, 1, 1))),
                new ResultBoundingBox(0, 0.99, fromOrigin(new Vertex(1, 1, 1))),
                new ResultBoundingBox(0, 0.49, fromOrigin(new Vertex(1, 1, 9))),
                new ResultBoundingBox(1, 1.00, fromOrigin(new Vertex(1, 1, 1))),
                new ResultBoundingBox(1, 0.99, fromOrigin(new Vertex(1, 1, 1))),
                new ResultBoundingBox(0, 0.49, fromOrigin(new Vertex(1, 1, 9)))
        );

        var result = new BoxFilter(0.5, 0.5).filter(boxes);
        assertEquals(List.of(boxes.get(0), boxes.get(3)), result);
    }
}