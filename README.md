# YOLO-3D for very large files

The aim of this project is to apply the well known YOLO object detection algorithm in 3D on very large files.

Typically, neural networks take input of a fixed size.
Therefore, additional work is needed to scan files that are too large to fit into the input shape of the network.

In this case, these files might even be larger than the RAM available to the program.

## Repository structure

- [/yolo-3d-poc](/yolo-3d-poc/)

  The main project.
  Java code to facilitate scanning of very large files. 
- [/yolo-3d-python](/yolo-3d-python)

  Contains python code used to train the model.
  This is much easier in python.
- [/saved-models](/saved-models)

  Pretrained models and weights.
- [/documents](/documents)

  Statistics, Reports and other documents created during development.

- [/datased-psb](/datased-psb)

  OFF models and classification information of the Princeton Shape Benchmark.
