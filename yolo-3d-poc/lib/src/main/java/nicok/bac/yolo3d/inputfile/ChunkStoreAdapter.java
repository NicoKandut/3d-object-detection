package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.storage.chunkstore.ChunkStore;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.preprocessing.Transformation;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class ChunkStoreAdapter implements InputFile {

    private final ChunkStore chunkStore;

    public ChunkStoreAdapter(final String path) throws IOException {
        requireNonNull(path);
        this.chunkStore = ChunkStore.reader(path);
    }

    @Override
    public Volume3D read(final BoundingBox target) {
        final var volume = Volume3D.forBoundingBox(target);

        for (var z = 0; z < target.size().z(); ++z) {
            for (var y = 0; y < target.size().y(); ++y) {
                for (var x = 0; x < target.size().x(); ++x) {
                    final var value = chunkStore.query(new Vertex(
                            target.min().x() + x,
                            target.min().y() + y,
                            target.min().z() + z
                    ));
                    volume.set(x, y, z, value);
                }
            }
        }

        return volume;
    }

    @Override
    public ChunkStore createChunkStore() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public InputFile transform(final Transformation transformation) {
        return this;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return chunkStore.boundingBox();
    }

    public void stats() {
        System.out.println("ChunkStoreAdapter stats");
        this.chunkStore.printStatistic();
    }
}
