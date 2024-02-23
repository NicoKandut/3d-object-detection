package nicok.bac.yolo3d.inputfile;

import com.scs.voxlib.*;
import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.common.Volume3D;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VoxAdapter implements InputFile {

    private final List<Voxel> voxels;
    private final int[] palette;


    public VoxAdapter(final String path) throws IOException {

        VoxReader reader = new VoxReader(new FileInputStream(path));
        final var voxFile = reader.read();

        palette = voxFile.getPalette();
        voxels = new ArrayList<>();

        for (VoxModelInstance model_instance : voxFile.getModelInstances()) {
            GridPoint3 world_Offset = model_instance.worldOffset;
            VoxModelBlueprint model = model_instance.model;
            for (Voxel voxel : model.getVoxels()) {
                final int x = world_Offset.x + voxel.getPosition().x;
                final int y = world_Offset.y + voxel.getPosition().y;
                final int z = world_Offset.z + voxel.getPosition().z;

                voxels.add(new Voxel(x, y, z, voxel.getColourIndexByte()));
            }
        }

        reader.close();

    }

    @Override
    public Volume3D read(final BoundingBox target) {
        final var volume = new Volume3D(
                (int) (target.max().x() - target.min().x()),
                (int) (target.max().y() - target.min().y()),
                (int) (target.max().z() - target.min().z())
        );

        for (final var v : voxels) {
            if (
                    v.getPosition().x >= target.min().x() && v.getPosition().x < target.max().x() &&
                            v.getPosition().y >= target.min().y() && v.getPosition().y < target.max().y() &&
                            v.getPosition().z >= target.min().z() && v.getPosition().z < target.max().z()
            ) {
                final var material = palette[v.getColourIndex()];
                final var r = (float) (material >> 24 & 0xFF);
                final var g = (float) (material >> 16 & 0xFF);
                final var b = (float) (material >> 8 & 0xFF);
                volume.set(v.getPosition().x, v.getPosition().y, v.getPosition().z, r, g, b);
            }
        }

        return volume;
    }

    @Override
    public BoundingBox getBoundingBox() {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float maxZ = Float.MIN_VALUE;

        for (final var v : voxels) {
            final var position = v.getPosition();
            minX = Float.min(minX, position.x);
            minY = Float.min(minY, position.y);
            minZ = Float.min(minZ, position.z);
            maxX = Float.max(maxX, position.x);
            maxY = Float.max(maxY, position.y);
            maxZ = Float.max(maxZ, position.z);
        }

        return new BoundingBox(
                new Point(minX, minY, minZ),
                new Point(maxX + 1, maxY + 1, maxZ + 1)
        );
    }
}
