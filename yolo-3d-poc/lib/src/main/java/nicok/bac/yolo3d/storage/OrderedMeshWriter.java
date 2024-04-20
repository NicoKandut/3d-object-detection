package nicok.bac.yolo3d.storage;

import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.Vertex;

import java.io.IOException;

public interface OrderedMeshWriter {
    void writeVertex(final Vertex vertex) throws IOException;

    void writeFace(final Face face) throws IOException;
}
