# Voxelization Test App

This app can be used to voxelize .off files.

The voxelized files can be converted to both a chunkstore and .vox files.
Chunkstores are used for easy access from other applications while VOX-files can be easily inspected with MagicaVoxel.

Of course the end goal is not to view files in MagicaVoxel but to feed them into a CNN.

## Arguments

- `-i` or `--input` (required): The input file.
- `-s` or `--size` (required): The size of the voxel grid. Format: 112,112,112
- `-f` or `--fit` (optional): Fit the model to the bounding box by scaling it up but preserving aspect ratios. Default is false.
- `-v` or `--vox` (optional): Saves a VOX file of the voxelized file. Useful for debugging.
- `-h` or `--help`: Show the help message.


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