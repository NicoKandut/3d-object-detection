package nicok.bac.yolo3d.network;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.common.Volume3D;

import java.util.List;

public interface Network {

    Point getExtent();

    List<ResultBoundingBox> compute(final BoundingBox box, final Volume3D volume);
}
