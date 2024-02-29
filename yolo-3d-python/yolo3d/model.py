
from keras.layers import BatchNormalization, Layer
from keras.layers import Conv3D, MaxPooling3D, Flatten, Dense, LeakyReLU
from keras.regularizers import l2
import keras.backend as K

def input_shape():
    return (28, 28, 28, 1)

class Yolo_Reshape(Layer):
    def __init__(self, target_shape, **kwargs):
        super(Yolo_Reshape, self).__init__(**kwargs)
        self.target_shape = tuple(target_shape)

    def compute_output_shape(self, input_shape):
        output_shape = (input_shape[0],) + self.target_shape
        return output_shape

    def call(self, inputs, **kwargs):
        S = [self.target_shape[0], self.target_shape[1], self.target_shape[2]]
        C = 2
        B = 1
        idx1 = S[0] * S[1] * S[2] * C
        idx2 = idx1 + S[0] * S[1] * S[2] * B

        # class prediction
        class_shape = (K.shape(inputs)[0],) + tuple([S[0], S[1], S[2], C])
        class_probs = K.reshape(inputs[:, :idx1], class_shape)
        class_probs = K.softmax(class_probs)

        # confidence
        confs_shape = (K.shape(inputs)[0],) + tuple([S[0], S[1], S[2], B])
        confs = K.reshape(inputs[:, idx1:idx2], confs_shape)
        confs = K.sigmoid(confs)

        # boxes
        boxes_shape = (K.shape(inputs)[0],) + tuple([S[0], S[1], S[2], B * 6])
        boxes = K.reshape(inputs[:, idx2:], boxes_shape)
        boxes = K.sigmoid(boxes)

        outputs = K.concatenate([class_probs, confs, boxes])

        return outputs
    
def conv_layer(id, x, filters):
    x = Conv3D(filters, (3, 3, 3), padding='same', name=f'convolutional_{id}', use_bias=False, kernel_regularizer=l2(5e-4), trainable=False)(x)
    x = BatchNormalization(name=f'bnconvolutional_{id}', trainable=False)(x)
    x = LeakyReLU(alpha=0.1)(x)
    return x

def pool_layer(x):
    x = MaxPooling3D((2, 2, 2), strides=(2, 2, 2), padding='same')(x)
    return x

def model_tiny_yolov1(inputs, num_classes=2, pooling_layers=2, output_size=7):
    x = inputs

    if pooling_layers >= 6:
        x = conv_layer('6', x, 16)
        x = pool_layer(x)

    if pooling_layers >= 5:
        x = conv_layer('5', x, 32)
        x = pool_layer(x)

    if pooling_layers >= 4:
        x = conv_layer('4', x, 64)
        x = pool_layer(x)

    if pooling_layers >= 3:
        x = conv_layer('3', x, 128)
        x = pool_layer(x)

    if pooling_layers >= 2:
        x = conv_layer('2', x, 256)
        x = pool_layer(x)

    if pooling_layers >= 1:
        x = conv_layer('1', x, 512)
        x = pool_layer(x)

    x = conv_layer('e1', x, 1024)
    x = conv_layer('e2', x, 256)

    x = Flatten()(x)
    x = Dense(output_size * output_size * output_size * (num_classes + 7), activation='linear', name='connected_0')(x)
    outputs = Yolo_Reshape((output_size, output_size, output_size, (num_classes + 7)))(x)

    return outputs