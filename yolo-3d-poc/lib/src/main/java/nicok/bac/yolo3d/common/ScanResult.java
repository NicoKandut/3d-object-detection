package nicok.bac.yolo3d.common;

import nicok.bac.yolo3d.collection.PersistentResultBoundingBoxList;

public record ScanResult(
        PersistentResultBoundingBoxList boxes
) {
}
