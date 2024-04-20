# App: Convert OFF to BFF

This app converts an .off file to a .bff file.

## Arguments

- `-i` or `--input` (required): The input file.
- `-o` or `--output` (required): The output file.
- `-h` or `--help`: Show the help message.

## Usage

With gradle:
```shell
gradlew run --args="-i input.off -o output.bff"
```

With jar:
```shell
java -jar app-off-to-bff.jar -i input.off -o output.bff
```

