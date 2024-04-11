package nicok.bac.yolo3d.storage.bff;

/**
 * Binary Object File Format (BFF) definitions.
 */
public final class BffFormat {

    /**
     * File type identifier.
     * First 4 bytes of the file.
     */
    public static final String FILE_TYPE = "BFF ";
    public static final long FILE_TYPE_BYTES = FILE_TYPE.getBytes().length;

    /**
     * Specifies number of bytes per vertex coordinate (or bounding box value) in the file.
     * 5th byte of the file.
     */
    public static final long PRECISION_TYPE_BYTES = 1;

    /**
     * Specifies number of bytes per vertex index.
     * 6th byte of the file.
     */
    public static final long INDEX_TYPE_BYTES = 1;

    /**
     * Header size in bytes.
     * Contains the file type, vertex count, face count, bounding box min and max.
     */
    public static final long HEADER_BYTES = FILE_TYPE_BYTES
            + PRECISION_TYPE_BYTES
            + INDEX_TYPE_BYTES
            + 3 * Long.BYTES
            + 6 * Double.BYTES;


    /**
     * Vertex size in bytes.
     * Each vertex has 3 values (x, y, z).
     * Actual size depends on the precision specified in the header.
     */
    public static long vertexBytes(final long precisionBytes) {
        return 3 * precisionBytes;
    }

    /**
     * Face size in bytes.
     * Each face has 3 indices values (v1, v2, v3).
     * Actual size depends on the index type specified in the header.
     */
    public static long faceBytes(final long indexBytes) {
        return 3 * indexBytes;
    }

    /**
     * Get the position (in bytes) of the vertex in the file.
     */
    public static long getVertexPosition(
            final long index,
            final long precisionBytes
    ) {
        return HEADER_BYTES
                + vertexBytes(precisionBytes) * index;
    }

    /**
     * Get the position (in bytes) of the face in the file.
     */
    public static long getFacePosition(
            final long index,
            final long precisionBytes,
            final long indexBytes,
            final long vertexCount
    ) {
        return HEADER_BYTES
                + vertexBytes(precisionBytes) * vertexCount
                + faceBytes(indexBytes) * index;
    }

    private BffFormat() {
        throw new UnsupportedOperationException("Utility class should not be instantiated.");
    }
}
