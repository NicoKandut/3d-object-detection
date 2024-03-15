package nicok.bac.yolo3d.network;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.off.Vertex;

import java.util.List;

public interface Network {

    Vertex getExtent();

    List<ResultBoundingBox> compute(final BoundingBox box, final Volume3D volume);
}
