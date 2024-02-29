package nicok.bac.yolo3d.vox;

import com.scs.voxlib.VoxFile;
import com.scs.voxlib.VoxWriter;
import com.scs.voxlib.chunk.VoxRootChunk;
import com.scs.voxlib.chunk.VoxSizeChunk;
import com.scs.voxlib.chunk.VoxXYZIChunk;
import nicok.bac.yolo3d.common.Volume3D;

import java.io.FileOutputStream;
import java.io.IOException;

public final class VoxFileUtil {
    public static void saveVoxFile(
            final String filename,
            final Volume3D volume
    ) {
        try (final var writer = new VoxWriter(new FileOutputStream(filename))) {
            final var root = new VoxRootChunk();

            final var size = volume.boundingBox().size();
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
}
