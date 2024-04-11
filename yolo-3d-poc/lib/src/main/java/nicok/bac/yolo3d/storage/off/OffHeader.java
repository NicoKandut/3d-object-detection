package nicok.bac.yolo3d.storage.off;

public record OffHeader(
        long vertexCount,
        long faceCount,
        long edgeCount
) {
}
