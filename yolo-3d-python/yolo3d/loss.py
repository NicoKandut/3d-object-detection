import argparse
from keras.layers import Input, BatchNormalization, Layer, GlobalAveragePooling3D
from keras.models import Model
from keras.callbacks import ModelCheckpoint, EarlyStopping, TensorBoard
from keras.layers import Conv3D, MaxPooling3D, \
    Flatten, Dense, Reshape, LeakyReLU
from keras.regularizers import l2
import keras.backend as K
import os

from .coordinates import xyzwhd_to_minmax

def iou(pred_mins, pred_maxes, true_mins, true_maxes):
    intersect_mins = K.maximum(pred_mins, true_mins)
    intersect_maxes = K.minimum(pred_maxes, true_maxes)
    intersect_whd = K.maximum(intersect_maxes - intersect_mins, 0.)
    intersect_volumes = intersect_whd[..., 0] * intersect_whd[..., 1] * intersect_whd[..., 2]

    pred_whd = pred_maxes - pred_mins
    true_whd = true_maxes - true_mins
    pred_volumes = pred_whd[..., 0] * pred_whd[..., 1] * pred_whd[..., 2]
    true_volumes = true_whd[..., 0] * true_whd[..., 1] * true_whd[..., 2]

    union_volumes = pred_volumes + true_volumes - intersect_volumes
    iou_scores = intersect_volumes / union_volumes
    iou_scores = K.expand_dims(iou_scores)

    return iou_scores

def create_grid_index(shape, index_type):
    conv_depth_index = K.arange(0, stop=shape[2])

    conv_height_index = K.arange(0, stop=shape[0])
    conv_height_index = K.expand_dims(conv_height_index, 0)
    conv_height_index = K.expand_dims(conv_height_index, 1)
    conv_height_index = K.tile(conv_height_index, [shape[2], shape[1], 1])
    
    conv_width_index = K.arange(0, stop=shape[1])
    conv_width_index = K.expand_dims(conv_width_index, 0)
    conv_width_index = K.expand_dims(conv_width_index, 2)
    conv_width_index = K.tile(conv_width_index, [shape[2], 1, shape[0]])

    conv_depth_index = K.arange(0, stop=shape[2])
    conv_depth_index = K.expand_dims(conv_depth_index, 1)
    conv_depth_index = K.expand_dims(conv_depth_index, 2)
    conv_depth_index = K.tile(conv_depth_index, [1, shape[1], shape[0]])
    
    conv_index = K.transpose(K.stack([conv_depth_index, conv_height_index, conv_width_index]))
    conv_index = K.reshape(conv_index, [1, shape[0], shape[1], shape[2], 3])
    conv_index = K.cast(conv_index, index_type)

    return conv_index

def yolo_head(feats, input_size=112, num_classes=48, num_channels=1):
    # Dynamic implementation of conv dims for fully convolutional model.
    conv_dims = (7,7,7)  # assuming channels last
    # In YOLO the height index is the inner most iteration.

    conv_index = create_grid_index(conv_dims, K.dtype(feats))
    conv_dims = K.cast(K.reshape(conv_dims, [1,1,1,1,3]), K.dtype(feats))

    box_xyz = (feats[..., :3] + conv_index) / conv_dims * input_size
    box_whd = feats[..., 3:6] * input_size

    return box_xyz, box_whd

def yolo_class_loss(y_true, y_pred, num_classes=48):
    label_class    = y_true[..., :num_classes] # ?x7x7x7x(num_classes)
    label_response = y_true[..., -1]           # ?x7x7x7x1

    predict_class  = y_pred[..., :num_classes] # ?x7x7x7x(num_classes)
    predict_trust  = y_pred[..., -1]           # ?x7x7x7x1

    response_mask  = K.expand_dims(label_response)
    predict_trust  = K.expand_dims(predict_trust)

    class_loss = response_mask * K.square(label_class - predict_class)

    return class_loss

def yolo_box_loss(y_true, y_pred, input_size=112, num_classes=48):
    # position of box information in output
    start = num_classes
    end = num_classes + 6

    label_box      = y_true[..., start:end] # ?x7x7x7x6
    label_response = y_true[..., -1]        # ?x7x7x7x1

    predict_box    = y_pred[..., start:end] # ?x7x7x7x6
    predict_trust  = y_pred[..., -1]        # ?x7x7x7x1
    
    response_mask  = K.expand_dims(label_response)
    predict_trust  = K.expand_dims(predict_trust)

    label_xyz, label_whd = yolo_head(label_box)       # both ?x7x7x7x3
    predict_xyz, predict_whd = yolo_head(predict_box) # both ?x7x7x7x3
 
    label_xyz_min, label_xyz_max = xyzwhd_to_minmax(label_xyz, label_whd)         # both ?x7x7x7x3
    predict_xyz_min, predict_xyz_max = xyzwhd_to_minmax(predict_xyz, predict_whd) # both ?x7x7x7x3

    iou_scores = iou(predict_xyz_min, predict_xyz_max, label_xyz_min, label_xyz_max)  # ? * 7 * 7 * 2 * 1
    best_box = K.max(iou_scores, axis=4, keepdims=True)  # ? * 7 * 7 * 1
    box_mask = K.cast(iou_scores >= best_box, K.dtype(iou_scores))  # ? * 7 * 7 * 2

    box_loss = 7 * box_mask * response_mask * K.square((label_xyz - predict_xyz) / input_size)
    box_loss += 7 * box_mask * response_mask * K.square((K.sqrt(label_whd) - K.sqrt(predict_whd)) / input_size)

    return box_loss


def yolo_confidence_loss(y_true, y_pred, input_size=112, num_classes=48):
    # position of box information in output
    start = num_classes
    end = num_classes + 6

    label_box      = y_true[..., start:end] # ?x7x7x7x6
    label_response = y_true[..., -1]        # ?x7x7x7x1

    predict_box    = y_pred[..., start:end] # ?x7x7x7x6
    predict_trust  = y_pred[..., -1]        # ?x7x7x7x1

    response_mask  = K.expand_dims(label_response)
    predict_trust  = K.expand_dims(predict_trust)

    label_xyz, label_whd = yolo_head(label_box)       # both ?x7x7x7x3
    predict_xyz, predict_whd = yolo_head(predict_box) # both ?x7x7x7x3
 
    label_xyz_min, label_xyz_max = xyzwhd_to_minmax(label_xyz, label_whd)         # both ?x7x7x7x3
    predict_xyz_min, predict_xyz_max = xyzwhd_to_minmax(predict_xyz, predict_whd) # both ?x7x7x7x3

    iou_scores = iou(predict_xyz_min, predict_xyz_max, label_xyz_min, label_xyz_max)  # ? * 7 * 7 * 2 * 1
    best_box = K.max(iou_scores, axis=4, keepdims=True)  # ? * 7 * 7 * 1
    box_mask = K.cast(iou_scores >= best_box, K.dtype(iou_scores))  # ? * 7 * 7 * 2

    no_object_loss = 0.5 * (1 - box_mask *  response_mask) * K.square(0 - predict_trust)
    object_loss = box_mask *  response_mask * K.square(1 - predict_trust)
    confidence_loss = no_object_loss + object_loss

    return confidence_loss

def yolo_loss(y_true, y_pred):
    confidence_loss = yolo_confidence_loss(y_true, y_pred)
    class_loss = yolo_class_loss(y_true, y_pred)
    box_loss = yolo_box_loss(y_true, y_pred)

    loss = K.sum(confidence_loss) + K.sum(class_loss) + K.sum(box_loss)

    return loss

