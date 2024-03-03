package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.preprocessing.LinearTransformation;
import nicok.bac.yolo3d.preprocessing.Transformation;

public interface InputFile {
    Volume3D read(final BoundingBox target);

    InputFile withPreprocessing(final Transformation preProcessing);

    BoundingBox getBoundingBox();
}
