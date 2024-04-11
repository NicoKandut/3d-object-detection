package nicok.bac.yolo3d.storage;

import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.Vertex;

public interface RandomAccessMeshReader {
    Vertex getVertex(final long index);
    Face getFace(final long index);
}
