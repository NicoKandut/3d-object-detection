package nicok.bac.yolo3d.bff;

public final class BffFormat {

    public static final String FILE_TYPE = "BFF ";
    public static final int FILE_TYPE_BYTES = 4;
    public static final int HEADER_BYTES = FILE_TYPE_BYTES + 3 * Integer.BYTES;
    public static final int VERTEX_BYTES = 3 * Double.BYTES;
    public static final int FACE_BYTES = 3 * Integer.BYTES;

    public static long getVertexPosition(final long index) {
        return HEADER_BYTES + VERTEX_BYTES * index;
    }

    public static long getFacePosition(final long index, final long vertexCount) {
        return HEADER_BYTES + VERTEX_BYTES * vertexCount + FACE_BYTES * index;
    }

    private BffFormat() {
    }
}
