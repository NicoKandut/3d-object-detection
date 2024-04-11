package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.preprocessing.Transformable;
import nicok.bac.yolo3d.storage.chunkstore.ChunkStore;

import java.io.IOException;

public interface InputFile extends Transformable<InputFile> {

    Volume3D read(final BoundingBox target);

    ChunkStore createChunkStore() throws IOException;

    BoundingBox getBoundingBox();
}
