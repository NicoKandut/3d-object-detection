# App: Shuffle BFF File

This app shuffles the order of vertices and faces in a .bff file.
This helps with testing the cache friendliness of the file.

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
java -jar app-shuffle-bff.jar -i input.bff
```

