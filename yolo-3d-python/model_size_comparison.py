from keras.layers import Input
from keras.models import Model

from yolo3d.model import model_tiny_yolov1
import numpy as np
import keras.backend as K

def fmt_memory(value):
    unit = "B"
    short_value = value

    if value >= (2 ** 40):
        unit = "TB"
        short_value = value / (2 ** 40)
    elif value >= (2 ** 30):
        unit = "GB"
        short_value = value / (2 ** 30)
    elif value >= (2 ** 20):
        unit = "MB"
        short_value = value / (2 ** 20)
    elif value >= (2 ** 10):
        unit = "KB"
        short_value = value / (2 ** 10)

    return f"{short_value:.1f} {unit}"
         

if __name__ == '__main__': 

    configurations = [
        (28, 2),
        (56, 3),
        (112, 4),
        (224, 5),
        (448, 6),
        (896, 7),
    ]

    for (size, pooling_layers) in configurations:
        input_shape = (size, size, size, 1)
        inputs = Input(input_shape)
        outputs = model_tiny_yolov1(inputs, pooling_layers=pooling_layers)
        model = Model(inputs=inputs, outputs=outputs)
        # model.summary()
        trainable_count = np.sum([K.count_params(w) for w in model.trainable_weights])
        non_trainable_count = np.sum([K.count_params(w) for w in model.non_trainable_weights])
        weight_count = trainable_count + non_trainable_count
        input_memory_size = fmt_memory(size ** 3)
        print(f'{pooling_layers:1d} pooling layers: Input: {size:3d}vx, Params: {non_trainable_count} + {trainable_count} = {weight_count}, input: {input_memory_size} bytes')
    