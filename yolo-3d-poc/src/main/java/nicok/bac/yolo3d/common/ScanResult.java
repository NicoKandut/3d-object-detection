package nicok.bac.yolo3d.common;

import java.util.List;

public record ScanResult(
       List<ResultBoundingBox> objects
) {
}
