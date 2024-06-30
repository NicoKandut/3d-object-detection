# App: Train

This app trains a model on the PSB dataset.

Actual training is done in python, this app just calls the python script.

The reason for this is that tensorflow training is much easier in python when compared to java.

## Arguments

- `e` or `--epochs` (required): The number of epochs to train the model on one dataset
- `se` or `--super-epochs` (required): The number of times a new dataset is loaded and the model is trained on it
- `v` or `--variations` (required): The number of variations per model
- `pd` or `--prepare-dataset`: Regenerate dataset files, if omitted, the app will use the existing dataset
- `h` or `--help`: Show the help message

## Usage

With gradle:
```shell
gradlew run --args="-e 10 -se 10 -v 10 -pd"
```

With jar:
```shell
java -jar app-train.jar -e 10 -se 10 -v 10 -pd
```

