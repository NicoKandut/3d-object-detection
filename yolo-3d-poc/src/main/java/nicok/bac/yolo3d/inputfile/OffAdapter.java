package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.common.Volume3D;

public class OffAdapter implements InputFile {

    public OffAdapter(final String path) {
    }

    @Override
    public Volume3D read(final BoundingBox target) {
        return null;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(
                new Point(-28, -28, -28),
                new Point(28, 28, 28)
        );
    }
}
