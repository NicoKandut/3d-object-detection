package nicok.bac.yolo3d.boundingbox;

import nicok.bac.yolo3d.mesh.Vertex;
import org.junit.jupiter.api.Test;

import static nicok.bac.yolo3d.mesh.Vertex.ORIGIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoundingBoxBuilderTest {

    @Test
    void itInitializesWithInvalidValues() {
        final var builder = new BoundingBox.Builder();
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void itExtendsAtLargerVertices() {
        final var builder = new BoundingBox.Builder();
        builder.withVertex(new Vertex(1, 2, 3));

        var box = builder.build();
        assertEquals(1, box.min().x());
        assertEquals(2, box.min().y());
        assertEquals(3, box.min().z());
        assertEquals(1, box.max().x());
        assertEquals(2, box.max().y());
        assertEquals(3, box.max().z());

        builder.withVertex(new Vertex(1, 2, 4));

        box = builder.build();
        assertEquals(1, box.min().x());
        assertEquals(2, box.min().y());
        assertEquals(3, box.min().z());
        assertEquals(1, box.max().x());
        assertEquals(2, box.max().y());
        assertEquals(4, box.max().z());

        builder.withVertex(new Vertex(17, 10, 4));

        box = builder.build();
        assertEquals(1, box.min().x());
        assertEquals(2, box.min().y());
        assertEquals(3, box.min().z());
        assertEquals(17, box.max().x());
        assertEquals(10, box.max().y());
        assertEquals(4, box.max().z());

        builder.withVertex(new Vertex(-1, -10, -67));

        box = builder.build();
        assertEquals(-1, box.min().x());
        assertEquals(-10, box.min().y());
        assertEquals(-67, box.min().z());
        assertEquals(17, box.max().x());
        assertEquals(10, box.max().y());
        assertEquals(4, box.max().z());

        // multiple vertices inside the box
        builder.withVertices(
                new Vertex(-1, -10, -67),
                new Vertex(0, -1, -56),
                new Vertex(2, 2, 2),
                ORIGIN
        );

        box = builder.build();
        assertEquals(-1, box.min().x());
        assertEquals(-10, box.min().y());
        assertEquals(-67, box.min().z());
        assertEquals(17, box.max().x());
        assertEquals(10, box.max().y());
        assertEquals(4, box.max().z());
    }

    @Test
    void itBecomesInvalidAfterReset() {
        final var builder = new BoundingBox.Builder();
        builder.withVertex(new Vertex(1, 2, 3));

        final var box = builder.build();
        assertEquals(1, box.min().x());
        assertEquals(2, box.min().y());
        assertEquals(3, box.min().z());
        assertEquals(1, box.max().x());
        assertEquals(2, box.max().y());
        assertEquals(3, box.max().z());

        builder.reset();

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void itChecksContainsTheSameWayBoundingBoxDoes() {
        final var builder = new BoundingBox.Builder();

        final var vertex1 = new Vertex(1, 2, 3);
        builder.withVertex(vertex1);
        var box = builder.build();
        assertEquals(box.contains(vertex1), builder.contains(vertex1));

        final var vertex2 = new Vertex(-1, -2, -3);
        builder.withVertex(vertex2);
        box = builder.build();
        assertEquals(box.contains(vertex2), builder.contains(vertex2));

        assertEquals(box.contains(ORIGIN), builder.contains(ORIGIN));

        final var vertex3 = new Vertex(1, 1, 1);
        assertEquals(box.contains(vertex3), builder.contains(vertex3));
        final var vertex4 = new Vertex(-1, 1, 1);
        assertEquals(box.contains(vertex4), builder.contains(vertex4));
        final var vertex5 = new Vertex(1, 1, -3);
        assertEquals(box.contains(vertex5), builder.contains(vertex5));
    }

}