# Voxelization Test App

This app can be used to voxelize .off files.

The voxelized files will be converted to .vox files.
These can be easily inspected with MagicaVoxel.

Of course the end goal is not to view files in MagicaVoxel but to feed them into a CNN.

## Usage

Example Usage

```shell
# simple usage
./gradlew :app-voxelize:run --args="--input=C:/src/bac/dataset-psb/db/7/m705/m705.off --output=C:/src/bac/assets-generated"

# shorthands
./gradlew :app-voxelize:run --args="-i C:/src/bac/dataset-psb/db/3/m348/m348.off -o C:/src/bac/assets-generated"

# shorthands
./gradlew :app-voxelize:run --args="-i C:/src/bac/dataset-psb/db/10/m1042/m1042.off -o C:/src/bac/assets-generated -s 2"
```