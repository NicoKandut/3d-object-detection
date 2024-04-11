package nicok.bac.yolo3d.boundingbox;

import nicok.bac.yolo3d.mesh.Vertex;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static nicok.bac.yolo3d.boundingbox.BoundingBox.fromOrigin;
import static nicok.bac.yolo3d.boundingbox.BoundingBox.withOffset;
import static nicok.bac.yolo3d.mesh.Vertex.ONE;
import static nicok.bac.yolo3d.mesh.Vertex.ORIGIN;
import static org.junit.jupiter.api.Assertions.*;

class BoundingBoxTest {

    @Nested
    class ConstructionTest {

        @Test
        void itRequiresNonNullVertices() {
            final var from = new Vertex(11, 9, 0);
            final var to = new Vertex(117, 122, 999);
            assertThrows(NullPointerException.class, () -> new BoundingBox(null, to));
            assertThrows(NullPointerException.class, () -> new BoundingBox(from, null));
        }

        @Test
        void itRequiresMinToBeSmallerOrEqualToMax() {
            final var a = new Vertex(117, 122, 999);
            final var b = new Vertex(11, 9, 0);
            assertThrows(IllegalArgumentException.class, () -> new BoundingBox(a, b));
        }

        @Test
        void itCreatesFromOriginWithVertex() {
            final var to = new Vertex(117, 122, 999);
            final var box = fromOrigin(to);
            assertEquals(ORIGIN, box.min());
            assertEquals(to, box.max());
        }

        @Test
        void itCreatesFromOriginWithSingleValue() {
            final var box = fromOrigin(117);
            assertEquals(ORIGIN, box.min());
            assertEquals(new Vertex(117, 117, 117), box.max());
        }
    }

    @Nested
    class GetTest {

        final BoundingBox ZERO = fromOrigin(ORIGIN);
        final BoundingBox COMMON = fromOrigin(new Vertex(112, 100, 17));
        final BoundingBox NEGATIVE = new BoundingBox(new Vertex(-10, -10, -10), new Vertex(1, 1, 1));

        @Test
        void itReturnsCenter() {
            assertEquals(new Vertex(0, 0, 0), ZERO.center());
            assertEquals(new Vertex(56, 50, 8.5), COMMON.center());
            assertEquals(new Vertex(-4.5, -4.5, -4.5), NEGATIVE.center());
        }

        @Test
        void itReturnsSize() {
            assertEquals(new Vertex(0, 0, 0), ZERO.size());
            assertEquals(new Vertex(112, 100, 17), COMMON.size());
            assertEquals(new Vertex(11, 11, 11), NEGATIVE.size());
        }

        @Test
        void itAddsOffsets() {
            final var offset = new Vertex(1, 2, 3);
            final var expected = new BoundingBox(
                    Vertex.add(COMMON.min(), offset),
                    Vertex.add(COMMON.max(), offset)
            );
            assertEquals(expected, withOffset(COMMON, offset));
        }

        @Nested
        class IntersectOverUnionTest {

            @Test
            void itCalculatesIntersectOverUnionForHalf() {
                final var a = new BoundingBox(new Vertex(0, 0, 0), new Vertex(1, 1, 1));
                final var b = new BoundingBox(new Vertex(0.5, 0, 0), new Vertex(1, 1, 1));
                assertEquals(0.5, BoundingBox.getIntersectOverUnion(a, b));
            }

            @Test
            void itCalculatesIntersectOverUnionForNonIntersectingBoxes() {
                final var a = new BoundingBox(new Vertex(0, 0, 0), new Vertex(1, 1, 1));
                final var b = new BoundingBox(new Vertex(1, 1, 1), new Vertex(3, 3, 3));
                assertEquals(0, BoundingBox.getIntersectOverUnion(a, b));
            }

            @Test
            void itCalculatesIntersectOverUnionForIdenticalBoxes() {
                final var a = new BoundingBox(new Vertex(0, 0, 0), new Vertex(1, 1, 1));
                assertEquals(1, BoundingBox.getIntersectOverUnion(a, a));
            }

            @Test
            void itCalculatesIntersectOverUnionForIntersectingBoxes() {
                final var a = new BoundingBox(new Vertex(0, 0, 0), new Vertex(1, 1, 1));
                final var b = new BoundingBox(new Vertex(0.2, 0.2, 0.2), new Vertex(1.2, 1.2, 1.2));
                assertEquals(0.3440860215053764, BoundingBox.getIntersectOverUnion(a, b));
            }
        }

        @Test
        void itChecksContainsForVertex() {
            final var vertex = new Vertex(0.5, 0.5, 0.5);
            final var vertex2 = new Vertex(20, 0.5, 0.0);
            final var vertex3 = new Vertex(0.5, -1.0, 0.5);

            assertFalse(ZERO.contains(ORIGIN));
            assertFalse(ZERO.contains(vertex));
            assertFalse(ZERO.contains(vertex2));
            assertFalse(ZERO.contains(vertex3));

            assertTrue(COMMON.contains(COMMON.min()));
            assertFalse(COMMON.contains(COMMON.max()));
            assertTrue(COMMON.contains(vertex));
            assertTrue(COMMON.contains(vertex2));
            assertFalse(COMMON.contains(vertex3));

            assertTrue(NEGATIVE.contains(NEGATIVE.min()));
            assertFalse(NEGATIVE.contains(NEGATIVE.max()));
            assertTrue(NEGATIVE.contains(ORIGIN));
            assertTrue(NEGATIVE.contains(vertex));
            assertTrue(NEGATIVE.contains(vertex3));
            assertFalse(NEGATIVE.contains(vertex2));
        }

        @Test
        void itChecksContainsForBox() {
            assertTrue(ZERO.contains(ZERO));
            assertTrue(COMMON.contains(COMMON));
            assertTrue(NEGATIVE.contains(NEGATIVE));

            assertTrue(COMMON.contains(ZERO));
            assertTrue(NEGATIVE.contains(ZERO));

            assertFalse(ZERO.contains(COMMON));
            assertFalse(ZERO.contains(NEGATIVE));
            assertFalse(COMMON.contains(NEGATIVE));
            assertFalse(NEGATIVE.contains(COMMON));
        }

        @Test
        void itImplementsToString() {
            assertEquals("[(0.0, 0.0, 0.0) to (1.0, 1.0, 1.0)]", fromOrigin(ONE).toString());
        }
    }
}