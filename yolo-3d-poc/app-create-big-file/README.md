# App: Create Big File

This app creates a big file with the specified size by combining multiple models.

## Arguments

- `-o` or `--output` (required): The output file.
- `-n` or `--number` (required): The number of models to put in the output file.
- `-ms` or `--model-sizes` (required): The sizes of the models to combine.
- `-s` or `--size` (required): The size of the output file.
- `-h` or `--help`: Show the help message.

## Usage

With gradle:
```shell
gradlew run --args="-o output.bff -n 4 -ms 80,120 -s 200,200,200"
```

With jar:
```shell
java -jar app-create-big-file.jar -o output.bff -n 4 -ms 80,120 -s 200,200,200
```

