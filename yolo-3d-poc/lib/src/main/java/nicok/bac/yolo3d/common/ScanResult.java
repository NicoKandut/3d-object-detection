package nicok.bac.yolo3d.common;

import nicok.bac.yolo3d.collection.PersistentResultBoundingBoxList;

/**
 * Represents the result of a scan.
 * @param boxes The bounding boxes.
 */
public record ScanResult(
        PersistentResultBoundingBoxList boxes
) {
}
