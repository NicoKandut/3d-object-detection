import tensorflow as tf

from keras import datasets, layers, models, backend
import matplotlib.pyplot as plt

num_classes = 20
classes = ["tree", "rock"]

# loss calculation from https://github.com/JY-112553/yolov1-keras-voc/blob/master/yolo/yolo.py

# whd = width, height, depth
def xyzwhd2minmax(xyz, whd):
    xyz_min = xyz - whd / 2
    xyz_max = xyz + whd / 2

    return xyz_min, xyz_max


def iou(pred_mins, pred_maxes, true_mins, true_maxes):
    intersect_mins = backend.maximum(pred_mins, true_mins)
    intersect_maxes = backend.minimum(pred_maxes, true_maxes)
    intersect_wh = backend.maximum(intersect_maxes - intersect_mins, 0.)
    intersect_areas = intersect_wh[..., 0] * intersect_wh[..., 1]

    pred_wh = pred_maxes - pred_mins
    true_wh = true_maxes - true_mins
    pred_areas = pred_wh[..., 0] * pred_wh[..., 1]
    true_areas = true_wh[..., 0] * true_wh[..., 1]

    union_areas = pred_areas + true_areas - intersect_areas
    iou_scores = intersect_areas / union_areas

    return iou_scores

def yolo_head(feats):
    # Dynamic implementation of conv dims for fully convolutional model.
    conv_dims = backend.shape(feats)[1:3]  # assuming channels last
    # In YOLO the height index is the inner most iteration.
    conv_height_index = backend.arange(0, stop=conv_dims[0])
    conv_width_index = backend.arange(0, stop=conv_dims[1])
    conv_height_index = backend.tile(conv_height_index, [conv_dims[1]])

    # TODO: Repeat_elements and tf.split doesn't support dynamic splits.
    # conv_width_index = backend.repeat_elements(conv_width_index, conv_dims[1], axis=0)
    conv_width_index = backend.tile(
        backend.expand_dims(conv_width_index, 0), [conv_dims[0], 1])
    conv_width_index = backend.flatten(backend.transpose(conv_width_index))
    conv_index = backend.transpose(backend.stack([conv_height_index, conv_width_index]))
    conv_index = backend.reshape(conv_index, [1, conv_dims[0], conv_dims[1], 1, 2])
    conv_index = backend.cast(conv_index, backend.dtype(feats))

    conv_dims = backend.cast(backend.reshape(conv_dims, [1, 1, 1, 1, 2]), K.dtype(feats))

    box_xy = (feats[..., :2] + conv_index) / conv_dims * 448
    box_wh = feats[..., 2:4] * 448

    return box_xy, box_wh


def yolo_loss(y_true, y_pred):
    label_class = y_true[..., :20]  # ? * 7 * 7 * 20
    label_box = y_true[..., 20:24]  # ? * 7 * 7 * 4
    response_mask = y_true[..., 24]  # ? * 7 * 7
    response_mask = backend.expand_dims(response_mask)  # ? * 7 * 7 * 1

    predict_class = y_pred[..., :20]  # ? * 7 * 7 * 20
    predict_trust = y_pred[..., 20:22]  # ? * 7 * 7 * 2
    predict_box = y_pred[..., 22:]  # ? * 7 * 7 * 8

    _label_box = backend.reshape(label_box, [-1, 7, 7, 1, 4])
    _predict_box = backend.reshape(predict_box, [-1, 7, 7, 2, 4])

    label_xyz, label_whd = yolo_head(_label_box)  # ? * 7 * 7 * 1 * 2, ? * 7 * 7 * 1 * 2
    label_xyz = backend.expand_dims(label_xyz, 4)  # ? * 7 * 7 * 1 * 1 * 2
    label_whd = backend.expand_dims(label_whd, 4)  # ? * 7 * 7 * 1 * 1 * 2
    label_xyz_min, label_xyz_max = xyzwhd2minmax(label_xyz, label_whd)  # ? * 7 * 7 * 1 * 1 * 2, ? * 7 * 7 * 1 * 1 * 2

    predict_xyz, predict_wh = yolo_head(_predict_box)  # ? * 7 * 7 * 2 * 2, ? * 7 * 7 * 2 * 2
    predict_xyz = backend.expand_dims(predict_xyz, 5)  # ? * 7 * 7 * 2 * 1 * 2
    predict_whd = backend.expand_dims(predict_whd, 5)  # ? * 7 * 7 * 2 * 1 * 2
    predict_xyz_min, predict_xyz_max = xyzwhd2minmax(predict_xyz, predict_whd)  # ? * 7 * 7 * 2 * 1 * 2, ? * 7 * 7 * 2 * 1 * 2

    iou_scores = iou(predict_xyz_min, predict_xyz_max, label_xyz_min, label_xyz_max)  # ? * 7 * 7 * 2 * 1
    best_ious = backend.max(iou_scores, axis=4)  # ? * 7 * 7 * 2
    best_box = backend.max(best_ious, axis=3, keepdims=True)  # ? * 7 * 7 * 1

    box_mask = backend.cast(best_ious >= best_box, backend.dtype(best_ious))  # ? * 7 * 7 * 2

    no_object_loss = 0.5 * (1 - box_mask * response_mask) * backend.square(0 - predict_trust)
    object_loss = box_mask * response_mask * backend.square(1 - predict_trust)
    confidence_loss = no_object_loss + object_loss
    confidence_loss = backend.sum(confidence_loss)

    class_loss = response_mask * backend.square(label_class - predict_class)
    class_loss = backend.sum(class_loss)

    _label_box = backend.reshape(label_box, [-1, 7, 7, 1, 4])
    _predict_box = backend.reshape(predict_box, [-1, 7, 7, 2, 4])

    label_xy, label_wh = yolo_head(_label_box)  # ? * 7 * 7 * 1 * 2, ? * 7 * 7 * 1 * 2
    predict_xy, predict_wh = yolo_head(_predict_box)  # ? * 7 * 7 * 2 * 2, ? * 7 * 7 * 2 * 2

    box_mask = backend.expand_dims(box_mask)
    response_mask = backend.expand_dims(response_mask)

    box_loss = 5 * box_mask * response_mask * backend.square((label_xy - predict_xy) / 448)
    box_loss += 5 * box_mask * response_mask * backend.square((backend.sqrt(label_wh) - backend.sqrt(predict_wh)) / 448)
    box_loss = backend.sum(box_loss)

    loss = confidence_loss + class_loss + box_loss

    return loss

model = models.Sequential()
# 4x
model.add(layers.Conv3D(256, kernel_size=(1,1,1), strides=(1,1,1), padding='same', activation='leaky_relu', input_shape=(28,28,28,3)))
model.add(layers.Conv3D(512, kernel_size=(3,3,3), strides=(1,1,1), padding='same', activation='leaky_relu'))
model.add(layers.Conv3D(256, kernel_size=(1,1,1), strides=(1,1,1), padding='same', activation='leaky_relu'))
model.add(layers.Conv3D(512, kernel_size=(3,3,3), strides=(1,1,1), padding='same', activation='leaky_relu'))
model.add(layers.Conv3D(256, kernel_size=(1,1,1), strides=(1,1,1), padding='same', activation='leaky_relu'))
model.add(layers.Conv3D(512, kernel_size=(3,3,3), strides=(1,1,1), padding='same', activation='leaky_relu'))
model.add(layers.Conv3D(256, kernel_size=(1,1,1), strides=(1,1,1), padding='same', activation='leaky_relu'))
model.add(layers.Conv3D(512, kernel_size=(3,3,3), strides=(1,1,1), padding='same', activation='leaky_relu'))

# unlabeled
model.add(layers.Conv3D(512,  kernel_size=(1,1,1), strides=(1,1,1), padding='same', activation='leaky_relu'))
model.add(layers.Conv3D(1024, kernel_size=(3,3,3), strides=(1,1,1), padding='same', activation='leaky_relu'))
model.add(layers.MaxPooling3D(pool_size=(2, 2, 2), strides=(2,2,2)))

# 2x
model.add(layers.Conv3D(512,  kernel_size=(1,1,1), strides=(1,1,1), padding='same', activation='leaky_relu'))
model.add(layers.Conv3D(1024, kernel_size=(3,3,3), strides=(1,1,1), padding='same', activation='leaky_relu'))
model.add(layers.Conv3D(512,  kernel_size=(1,1,1), strides=(1,1,1), padding='same', activation='leaky_relu'))
model.add(layers.Conv3D(1024, kernel_size=(3,3,3), strides=(1,1,1), padding='same', activation='leaky_relu'))

#unlabeled
model.add(layers.Conv3D(1024, kernel_size=(3,3,3), strides=(1,1,1), padding='same', activation='leaky_relu'))
model.add(layers.Conv3D(1024, kernel_size=(3,3,3), strides=(2,2,2), padding='same', activation='leaky_relu'))
model.add(layers.Flatten())
model.add(layers.Dense(units=4096))
# model.add(layers.Dropout(0.5))
# model.add(layers.Dense(7*7*7*25))
# model.add(layers.Reshape((7, 7, 7, 25)))

model.summary()

model.compile(optimizer='adam', loss=yolo_loss, metrics=['accuracy'])
# model.fit(X_train, y_train, epochs=10, batch_size=32, validation_data=(X_val, y_val))

print("DONE!!!!!!!!!!!!!!!!!!!!!!!!!!!!")