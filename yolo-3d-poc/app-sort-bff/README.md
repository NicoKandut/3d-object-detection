# App: Sort BFF File

This app sorts the order of vertices and faces in a .bff file.
The sorting is done by the vertex z coordinates.
For faces, the sorting is done by the minimum z coordinate of the face vertices.

## Arguments

- `-i` or `--input` (required): The input file.
- `-h` or `--help`: Show the help message.

## Usage

With gradle:
```shell
gradlew run --args="-i input.bff"
```

With jar:
```shell
java -jar app-sort-bff.jar -i input.bff
```

