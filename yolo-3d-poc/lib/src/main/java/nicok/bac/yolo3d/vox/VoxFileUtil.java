package nicok.bac.yolo3d.vox;

import com.scs.voxlib.VoxFile;
import com.scs.voxlib.VoxWriter;
import com.scs.voxlib.Voxel;
import com.scs.voxlib.chunk.VoxRootChunk;
import com.scs.voxlib.chunk.VoxSizeChunk;
import com.scs.voxlib.chunk.VoxXYZIChunk;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.storage.chunkstore.ChunkStore;
import org.apache.commons.math3.util.Pair;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class VoxFileUtil {
    public static void saveVoxFile(
            final String filename,
            final Volume3D volume
    ) {
        try (final var writer = new VoxWriter(new FileOutputStream(filename))) {
            final var root = new VoxRootChunk();

            final var size = volume.getBoundingBox().size();
            final var sizeChunk = new VoxSizeChunk((int) Math.ceil(size.x()), (int) Math.ceil(size.y()), (int) Math.ceil(size.z()));
            root.appendChunk(sizeChunk);

            final var voxels = volume.toVoxels();
            final var chunk = new VoxXYZIChunk(voxels);
            root.appendChunk(chunk);

            writer.write(new VoxFile(VoxWriter.VERSION, root));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveVoxFile(
            final String filename,
            final ChunkStore chunkStore
    ) {
        try (final var writer = new VoxWriter(new FileOutputStream(filename))) {
            final var root = new VoxRootChunk();

            final var size = chunkStore.boundingBox().size();
            final var sizeChunk = new VoxSizeChunk((int) Math.ceil(size.x()), (int) Math.ceil(size.y()), (int) Math.ceil(size.z()));
            root.appendChunk(sizeChunk);

            final var voxels = toVoxels(chunkStore);
            final var chunk = new VoxXYZIChunk(voxels);
            root.appendChunk(chunk);

            writer.write(new VoxFile(VoxWriter.VERSION, root));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Voxel> toVoxels(final ChunkStore chunkStore) {
//        final var size = chunkStore.boundingBox().size();
        final var voxels = new ArrayList<Voxel>();

        try {
            chunkStore.queryAll()
                    .filter(pair -> pair.getValue() != 0f)
                    .map(Pair::getKey)
                    .map(vertex -> new Voxel((int) vertex.x(), (int) vertex.y(), (int) vertex.z(), (byte) 80))
                    .forEach(voxels::add);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        for (var i = 0; i < size.x(); ++i) {
//            for (var j = 0; j < size.y(); ++j) {
//                for (var k = 0; k < size.z(); ++k) {
//                    final var value = chunkStore.query(new Vertex(i, j, k));
//                    if (value != 0f) {
//                        voxels.add(new Voxel(i, j, k, (byte) 80));
//                    }
//                }
//            }
//        }
        return voxels;
    }
}
