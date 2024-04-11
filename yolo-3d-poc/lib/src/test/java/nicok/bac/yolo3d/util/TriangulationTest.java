package nicok.bac.yolo3d.util;

import nicok.bac.yolo3d.mesh.Face;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TriangulationTest {

    @Nested
    class ShellTest {

        @Test
        void itThrowsForTooFewVertices() {
            final var face = new Face(List.of(1L, 2L));
            assertThrows(IllegalArgumentException.class, () -> Triangulation.shell(face));
        }

        @Test
        void itWorksForSquares() {
            final var indices = List.of(17L, 1L, 13L, 101L);
            final var face = new Face(indices);
            final var faces = Triangulation.shell(face).toList();
            assertEquals(2, faces.size());

            final var face1 = faces.get(0);
            assertEquals(indices.get(0), face1.index1());
            assertEquals(indices.get(1), face1.index2());
            assertEquals(indices.get(2), face1.index3());

            final var face2 = faces.get(1);
            assertEquals(indices.get(0), face2.index1());
            assertEquals(indices.get(2), face2.index2());
            assertEquals(indices.get(3), face2.index3());
        }

        @Test
        void itWorksWithManyVertices() {
            final var indices = List.of(1L, 2L, 3L, 5L, 8L, 13L, 21L);
            final var face = new Face(indices);
            final var faces = Triangulation.shell(face).toList();
            assertEquals(5, faces.size());

            final var face1 = faces.get(0);
            assertEquals(indices.get(0), face1.index1());
            assertEquals(indices.get(1), face1.index2());
            assertEquals(indices.get(2), face1.index3());

            final var face2 = faces.get(1);
            assertEquals(indices.get(0), face2.index1());
            assertEquals(indices.get(2), face2.index2());
            assertEquals(indices.get(3), face2.index3());

            final var face3 = faces.get(2);
            assertEquals(indices.get(0), face3.index1());
            assertEquals(indices.get(3), face3.index2());
            assertEquals(indices.get(4), face3.index3());

            final var face4 = faces.get(3);
            assertEquals(indices.get(0), face4.index1());
            assertEquals(indices.get(4), face4.index2());
            assertEquals(indices.get(5), face4.index3());

            final var face5 = faces.get(4);
            assertEquals(indices.get(0), face5.index1());
            assertEquals(indices.get(5), face5.index2());
            assertEquals(indices.get(6), face5.index3());
        }
    }
}