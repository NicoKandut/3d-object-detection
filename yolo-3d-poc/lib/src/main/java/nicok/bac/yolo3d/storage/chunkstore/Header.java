package nicok.bac.yolo3d.storage.chunkstore;

import nicok.bac.yolo3d.boundingbox.BoundingBox;

public record Header(
        BoundingBox boundingBox,
        int chunkSize
) {
}
