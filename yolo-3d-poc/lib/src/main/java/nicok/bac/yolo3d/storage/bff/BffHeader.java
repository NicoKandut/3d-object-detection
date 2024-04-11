package nicok.bac.yolo3d.storage.bff;

import nicok.bac.yolo3d.boundingbox.BoundingBox;

public record BffHeader(
        long precisionBytes,
        long indexBytes,
        long vertexCount,
        long faceCount,
        long edgeCount,
        BoundingBox boundingBox
) {
}
