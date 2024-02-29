package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Volume3D;

public interface InputFile {
    Volume3D read(final BoundingBox target);

    BoundingBox getBoundingBox();
}
