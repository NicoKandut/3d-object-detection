package nicok.bac.yolo3d.scanner;

import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.common.Volume3D;

import java.util.List;

public interface Network {

    Point getExtent();

    List<ResultBoundingBox> compute(Volume3D volume);
}
