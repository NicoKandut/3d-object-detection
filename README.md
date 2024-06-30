# YOLO-3D for very large files

The aim of this project is to apply the well known YOLO object detection algorithm in 3D on very large files.

Typically, neural networks take input of a fixed size.
Therefore, additional work is needed to scan files that are too large to fit into the input shape of the network.

In this case, these files might even be larger than the RAM available to the program.

## Repository structure

- [yolo-3d-poc](./yolo-3d-poc)

  The main project.
  Java code to facilitate scanning of very large files. 

- [yolo-3d-python](./yolo-3d-python)

  Contains python code used to train the model.
  This is much easier in python.

- [saved-models](./saved-models)

  Pretrained models and weights.

- [documents](./documents)

  Statistics, Reports and other documents created during development.

- [dataset-psb](./dataset-psb)

  OFF models and classification information of the Princeton Shape Benchmark.

## Getting started

While the most interesting parts of this repository are located in [yolo-3d-poc](./yolo-3d-poc) and [yolo-3d-python](./yolo-3d-python), we recommend to first familiarize yourself with the [Princeton Shape Benchmark](http://shape.cs.princeton.edu/benchmark/) and OFF files.

You can find a few such files in the [assets](./assets) directory.

## Working with the project

The project is split into two parts: The Java part and the Python part.

### Java

The Java part is located in the [yolo-3d-poc](./yolo-3d-poc) directory.
It uses gradle as a build system and is therefore easy to run.

For more details check the [README](./yolo-3d-poc/README.md) in the respective directory.

### Python

The Python part is located in the [yolo-3d-python](./yolo-3d-python) directory.
It uses python and the keras library to train the model.
While training in Java is possible, it is much easier to do in Python.
At the time of writing, the Java implementation of Tensorflow is not as feature complete as the Python one.

For more details check the [README](./yolo-3d-python/README.md) in the respective directory.
