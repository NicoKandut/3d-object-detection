# App: Traverse BFF File

This app traverses a .bff file to calculate cache statistics and validate the implementation of reader and writer.

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
java -jar app-traverse-bff-file.jar -i input.bff
```

