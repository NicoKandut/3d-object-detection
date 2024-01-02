
from keras.layers import BatchNormalization, Layer
from keras.layers import Conv3D, MaxPooling3D, \
    Flatten, Dense, LeakyReLU
from keras.regularizers import l2
import keras.backend as K


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


def model_tiny_yolov1(inputs, num_classes):
    # x = Conv2D(16, (3, 3), padding='same', name='convolutional_0', use_bias=False,
    #            kernel_regularizer=l2(5e-4), trainable=False)(inputs)
    # x = BatchNormalization(name='bnconvolutional_0', trainable=False)(x)
    # x = LeakyReLU(alpha=0.1)(x)
    # x = MaxPooling2D((2, 2), strides=(2, 2), padding='same')(x)

    # x = Conv2D(32, (3, 3), padding='same', name='convolutional_1', use_bias=False,
    #            kernel_regularizer=l2(5e-4), trainable=False)(x)
    # x = BatchNormalization(name='bnconvolutional_1', trainable=False)(x)
    # x = LeakyReLU(alpha=0.1)(x)
    # x = MaxPooling2D((2, 2), strides=(2, 2), padding='same')(x)

    # x = Conv2D(64, (3, 3), padding='same', name='convolutional_2', use_bias=False,
    #            kernel_regularizer=l2(5e-4), trainable=False)(x)
    # x = BatchNormalization(name='bnconvolutional_2', trainable=False)(x)
    # x = LeakyReLU(alpha=0.1)(x)
    # x = MaxPooling2D((2, 2), strides=(2, 2), padding='same')(x)

    # x = Conv2D(128, (3, 3), padding='same', name='convolutional_3', use_bias=False,
    #            kernel_regularizer=l2(5e-4), trainable=False)(x)
    # x = BatchNormalization(name='bnconvolutional_3', trainable=False)(x)
    # x = LeakyReLU(alpha=0.1)(x)
    # x = MaxPooling2D((2, 2), strides=(2, 2), padding='same')(x)

    x = Conv3D(256, (3, 3, 3), padding='same', name='convolutional_4', use_bias=False, kernel_regularizer=l2(5e-4), trainable=False)(inputs)
    x = BatchNormalization(name='bnconvolutional_4', trainable=False)(x)
    x = LeakyReLU(alpha=0.1)(x)
    x = MaxPooling3D((2, 2, 2), strides=(2, 2, 2), padding='same')(x)

    x = Conv3D(512, (3, 3, 3), padding='same', name='convolutional_5', use_bias=False, kernel_regularizer=l2(5e-4), trainable=False)(x)
    x = BatchNormalization(name='bnconvolutional_5', trainable=False)(x)
    x = LeakyReLU(alpha=0.1)(x)
    x = MaxPooling3D((2, 2, 2), strides=(2, 2, 2), padding='same')(x)

    x = Conv3D(1024, (3, 3, 3), padding='same', name='convolutional_6', use_bias=False, kernel_regularizer=l2(5e-4), trainable=False)(x)
    x = BatchNormalization(name='bnconvolutional_6', trainable=False)(x)
    x = LeakyReLU(alpha=0.1)(x)

    x = Conv3D(256, (3, 3, 3), padding='same', name='convolutional_7', use_bias=False, kernel_regularizer=l2(5e-4), trainable=False)(x)
    x = BatchNormalization(name='bnconvolutional_7', trainable=False)(x)
    x = LeakyReLU(alpha=0.1)(x)

    x = Flatten()(x)
    x = Dense(7 * 7 * 7 * (num_classes + 7), activation='linear', name='connected_0')(x)
    outputs = Yolo_Reshape((7, 7, 7, (num_classes + 7)))(x)

    return outputs