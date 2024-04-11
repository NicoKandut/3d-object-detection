package nicok.bac.yolo3d.mesh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VertexTest {

    @Test
    public void testAdd() {
        Vertex v1 = new Vertex(1, 2, 3);
        Vertex v2 = new Vertex(4, 5, 6);
        Vertex result = Vertex.add(v1, v2);
        assertEquals(new Vertex(5, 7, 9), result);
    }

    @Test
    public void testSub() {
        Vertex v1 = new Vertex(1, 2, 3);
        Vertex v2 = new Vertex(4, 5, 6);
        Vertex result = Vertex.sub(v1, v2);
        assertEquals(new Vertex(-3, -3, -3), result);
    }

    @Test
    public void testMul() {
        Vertex v = new Vertex(1, 2, 3);
        Vertex result = Vertex.mul(v, 2);
        assertEquals(new Vertex(2, 4, 6), result);
    }

    @Test
    public void testDiv() {
        Vertex v = new Vertex(2, 4, 6);
        Vertex result = Vertex.div(v, 2);
        assertEquals(new Vertex(1, 2, 3), result);
    }

    @Test
    public void testComponentWiseMultiply() {
        Vertex v1 = new Vertex(1, 2, 3);
        Vertex v2 = new Vertex(4, 5, 6);
        Vertex result = Vertex.componentWiseMultiply(v1, v2);
        assertEquals(new Vertex(4, 10, 18), result);
    }

    @Test
    public void testComponentWiseDiv() {
        Vertex v1 = new Vertex(4, 10, 18);
        Vertex v2 = new Vertex(4, 5, 6);
        Vertex result = Vertex.componentWiseDiv(v1, v2);
        assertEquals(new Vertex(1, 2, 3), result);
    }

    @Test
    public void testMin() {
        Vertex v1 = new Vertex(1, 2, 3);
        Vertex v2 = new Vertex(4, 5, 6);
        Vertex result = Vertex.min(v1, v2);
        assertEquals(new Vertex(1, 2, 3), result);
    }

    @Test
    public void testMax() {
        Vertex v1 = new Vertex(1, 2, 3);
        Vertex v2 = new Vertex(4, 5, 6);
        Vertex result = Vertex.max(v1, v2);
        assertEquals(new Vertex(4, 5, 6), result);
    }

    @Test
    public void testDot() {
        Vertex v1 = new Vertex(1, 2, 3);
        Vertex v2 = new Vertex(4, 5, 6);
        double result = Vertex.dot(v1, v2);
        assertEquals(32, result);
    }

    @Test
    public void testCross() {
        Vertex v1 = new Vertex(1, 2, 3);
        Vertex v2 = new Vertex(4, 5, 6);
        Vertex result = Vertex.cross(v1, v2);
        assertEquals(new Vertex(-3, 6, -3), result);
    }

    @Test
    public void testNormalize() {
        Vertex v = new Vertex(1, 2, 3);
        Vertex result = v.normalize();
        double length = Math.sqrt(14);
        assertEquals(new Vertex(1 / length, 2 / length, 3 / length), result);
    }

    @Test
    public void testFloor() {
        final var v = new Vertex(1.1, 2.0, 3.99);
        assertEquals(new Vertex(1, 2, 3), Vertex.floor(v));
    }

    @Test
    public void testRound() {
        final var v = new Vertex(1.49, 2.5, 5);
        assertEquals(new Vertex(1, 3, 5), Vertex.round(v));
    }

    @Test
    public void testCeil() {
        final var v = new Vertex(1.01, 2, 3.99);
        assertEquals(new Vertex(2, 2, 4), Vertex.ceil(v));
    }
}