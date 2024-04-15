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
        (14, 1),
        (28, 2),
        (56, 3),
        (112, 4),
        (224, 5),
        (448, 6),
        (896, 7),
    ]

    # print("Model Size for different input sizes")
    # for (size, pooling_layers) in configurations:
    #     input_shape = (size, size, size, 1)
    #     inputs = Input(input_shape)
    #     outputs = model_tiny_yolov1(inputs, pooling_layers=pooling_layers, num_classes=3)
    #     model = Model(inputs=inputs, outputs=outputs)
    #     trainable_count = np.sum([K.count_params(w) for w in model.trainable_weights])
    #     non_trainable_count = np.sum([K.count_params(w) for w in model.non_trainable_weights])
    #     weight_count = trainable_count + non_trainable_count
    #     input_size_raw = size ** 3
    #     input_memory_size = fmt_memory(input_size_raw)
    #     print(f'{pooling_layers:1d} pooling layers: Input: {size:3d}vx, Params: {non_trainable_count} + {trainable_count} = {weight_count}, input: {input_size_raw:9d} ({input_memory_size}) bytes')

    # 1 pooling layers: Input:  14vx, Params: 21254656 + 271066383 = 292321039, input:      2744 (2.7 KB) bytes
    # 2 pooling layers: Input:  28vx, Params: 24787712 + 271066383 = 295854095, input:     21952 (21.4 KB) bytes
    # 3 pooling layers: Input:  56vx, Params: 25669504 + 271066383 = 296735887, input:    175616 (171.5 KB) bytes
    # 4 pooling layers: Input: 112vx, Params: 25889216 + 271066383 = 296955599, input:   1404928 (1.3 MB) bytes
    # 5 pooling layers: Input: 224vx, Params: 25943776 + 271066383 = 297010159, input:  11239424 (10.7 MB) bytes
    # 6 pooling layers: Input: 448vx, Params: 25957232 + 271066383 = 297023615, input:  89915392 (85.8 MB) bytes
    # 7 pooling layers: Input: 896vx, Params: 25960504 + 271066383 = 297026887, input: 719323136 (686.0 MB) bytes
        
    print("Model Size for different number of categories")
    size, pooling_layers = configurations[3]    
    print(f"Using {pooling_layers} pooling layers: {size}vx")
    for num_classes in range(20, 50, 3):
        input_shape = (size, size, size, 1)
        inputs = Input(input_shape)
        outputs = model_tiny_yolov1(inputs, num_classes=num_classes, pooling_layers=pooling_layers)
        model = Model(inputs=inputs, outputs=outputs)
        trainable_count = np.sum([K.count_params(w) for w in model.trainable_weights])
        non_trainable_count = np.sum([K.count_params(w) for w in model.non_trainable_weights])
        weight_count = trainable_count + non_trainable_count
        input_size_raw = size ** 3
        input_memory_size = fmt_memory(input_size_raw)
        print(f'{num_classes:2d} classes - Params: {non_trainable_count} fixed + {trainable_count} trained = {weight_count} total')
        model.summary()
        print("")
        print("")

    #  2 classes - Params: 25960504 fixed +  793359 trained = 26753863 total
    #  3 classes - Params: 25960504 fixed +  881510 trained = 26842014 total
    #  4 classes - Params: 25960504 fixed +  969661 trained = 26930165 total
    #  5 classes - Params: 25960504 fixed + 1057812 trained = 27018316 total
    #  6 classes - Params: 25960504 fixed + 1145963 trained = 27106467 total
    #  7 classes - Params: 25960504 fixed + 1234114 trained = 27194618 total
    #  8 classes - Params: 25960504 fixed + 1322265 trained = 27282769 total
    #  9 classes - Params: 25960504 fixed + 1410416 trained = 27370920 total
    # 10 classes - Params: 25960504 fixed + 1498567 trained = 27459071 total
    # 11 classes - Params: 25960504 fixed + 1586718 trained = 27547222 total
    # 12 classes - Params: 25960504 fixed + 1674869 trained = 27635373 total
    # 13 classes - Params: 25960504 fixed + 1763020 trained = 27723524 total
    # 14 classes - Params: 25960504 fixed + 1851171 trained = 27811675 total
    # 15 classes - Params: 25960504 fixed + 1939322 trained = 27899826 total
    # 16 classes - Params: 25960504 fixed + 2027473 trained = 27987977 total
    # 17 classes - Params: 25960504 fixed + 2115624 trained = 28076128 total
    # 18 classes - Params: 25960504 fixed + 2203775 trained = 28164279 total
    # 19 classes - Params: 25960504 fixed + 2291926 trained = 28252430 total
    # 20 classes - Params: 25960504 fixed + 2380077 trained = 28340581 total
    # 21 classes - Params: 25960504 fixed + 2468228 trained = 28428732 total
    # 22 classes - Params: 25960504 fixed + 2556379 trained = 28516883 total
    # 23 classes - Params: 25960504 fixed + 2644530 trained = 28605034 total
    # 24 classes - Params: 25960504 fixed + 2732681 trained = 28693185 total
    # 25 classes - Params: 25960504 fixed + 2820832 trained = 28781336 total
    # 26 classes - Params: 25960504 fixed + 2908983 trained = 28869487 total
    # 27 classes - Params: 25960504 fixed + 2997134 trained = 28957638 total
    # 28 classes - Params: 25960504 fixed + 3085285 trained = 29045789 total
    # 29 classes - Params: 25960504 fixed + 3173436 trained = 29133940 total
    # 30 classes - Params: 25960504 fixed + 3261587 trained = 29222091 total
    # 31 classes - Params: 25960504 fixed + 3349738 trained = 29310242 total
    # 32 classes - Params: 25960504 fixed + 3437889 trained = 29398393 total
    # 33 classes - Params: 25960504 fixed + 3526040 trained = 29486544 total
    # 34 classes - Params: 25960504 fixed + 3614191 trained = 29574695 total
    # 35 classes - Params: 25960504 fixed + 3702342 trained = 29662846 total
    # 36 classes - Params: 25960504 fixed + 3790493 trained = 29750997 total
    # 37 classes - Params: 25960504 fixed + 3878644 trained = 29839148 total
    # 38 classes - Params: 25960504 fixed + 3966795 trained = 29927299 total
    # 39 classes - Params: 25960504 fixed + 4054946 trained = 30015450 total
    # 40 classes - Params: 25960504 fixed + 4143097 trained = 30103601 total
    # 41 classes - Params: 25960504 fixed + 4231248 trained = 30191752 total
    # 42 classes - Params: 25960504 fixed + 4319399 trained = 30279903 total
    # 43 classes - Params: 25960504 fixed + 4407550 trained = 30368054 total
    # 44 classes - Params: 25960504 fixed + 4495701 trained = 30456205 total
    # 45 classes - Params: 25960504 fixed + 4583852 trained = 30544356 total
    # 46 classes - Params: 25960504 fixed + 4672003 trained = 30632507 total
    # 47 classes - Params: 25960504 fixed + 4760154 trained = 30720658 total
    # 48 classes - Params: 25960504 fixed + 4848305 trained = 30808809 total
    # 49 classes - Params: 25960504 fixed + 4936456 trained = 30896960 total
    # 50 classes - Params: 25960504 fixed + 5024607 trained = 30985111 total
    # 51 classes - Params: 25960504 fixed + 5112758 trained = 31073262 total
    # 52 classes - Params: 25960504 fixed + 5200909 trained = 31161413 total
    # 53 classes - Params: 25960504 fixed + 5289060 trained = 31249564 total
    # 54 classes - Params: 25960504 fixed + 5377211 trained = 31337715 total
    # 55 classes - Params: 25960504 fixed + 5465362 trained = 31425866 total
    # 56 classes - Params: 25960504 fixed + 5553513 trained = 31514017 total
    # 57 classes - Params: 25960504 fixed + 5641664 trained = 31602168 total
    # 58 classes - Params: 25960504 fixed + 5729815 trained = 31690319 total
    # 59 classes - Params: 25960504 fixed + 5817966 trained = 31778470 total
    # 60 classes - Params: 25960504 fixed + 5906117 trained = 31866621 total
    # 61 classes - Params: 25960504 fixed + 5994268 trained = 31954772 total
    # 62 classes - Params: 25960504 fixed + 6082419 trained = 32042923 total
    # 63 classes - Params: 25960504 fixed + 6170570 trained = 32131074 total
    # 64 classes - Params: 25960504 fixed + 6258721 trained = 32219225 total
    # 65 classes - Params: 25960504 fixed + 6346872 trained = 32307376 total
    # 66 classes - Params: 25960504 fixed + 6435023 trained = 32395527 total
    # 67 classes - Params: 25960504 fixed + 6523174 trained = 32483678 total
    # 68 classes - Params: 25960504 fixed + 6611325 trained = 32571829 total
    # 69 classes - Params: 25960504 fixed + 6699476 trained = 32659980 total
    # 70 classes - Params: 25960504 fixed + 6787627 trained = 32748131 total
    # 71 classes - Params: 25960504 fixed + 6875778 trained = 32836282 total
    # 72 classes - Params: 25960504 fixed + 6963929 trained = 32924433 total
    # 73 classes - Params: 25960504 fixed + 7052080 trained = 33012584 total
    # 74 classes - Params: 25960504 fixed + 7140231 trained = 33100735 total
    # 75 classes - Params: 25960504 fixed + 7228382 trained = 33188886 total
    # 76 classes - Params: 25960504 fixed + 7316533 trained = 33277037 total
    # 77 classes - Params: 25960504 fixed + 7404684 trained = 33365188 total
    # 78 classes - Params: 25960504 fixed + 7492835 trained = 33453339 total
    # 79 classes - Params: 25960504 fixed + 7580986 trained = 33541490 total