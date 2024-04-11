package nicok.bac.yolo3d.storage;

import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.Vertex;

public interface RandomAccessMeshWriter {
    void setVertex(final long index, Vertex vertex);
    void setFace(final long index, Face face);
}
