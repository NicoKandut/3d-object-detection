# App: Scan Big File

This app scans a big file in chunks to detect objects.

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
java -jar app-scan-big-file.jar -i input.bff
```

