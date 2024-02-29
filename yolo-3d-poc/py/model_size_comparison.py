from keras.layers import Input
from keras.models import Model

from yolo3d.model import model_tiny_yolov1
import numpy as np

if __name__ == '__main__': 
    for i in range(4):
        output_size = 1
        color_channels = 1
        size = output_size * (2 ** i)
        input_shape = (size, size, size, color_channels)
        inputs = Input(input_shape)
        outputs = model_tiny_yolov1(inputs, i, output_size)
        model = Model(inputs=inputs, outputs=outputs)
        import keras.backend as K
        trainable_count = np.sum([K.count_params(w) for w in model.trainable_weights])
        print(f'{i}: Input: {size}vx, Trainable params: {trainable_count}')
    