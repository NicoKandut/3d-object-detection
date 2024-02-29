import sys
import keras.backend as K
import numpy as np

from keras.layers import Input
from keras.models import Model
import tensorflow as tf

from pyvox.parser import VoxParser
from pyvox.writer import VoxWriter
from pyvox.models import Model as VoxModel, Voxel

from yolo3d.model import model_tiny_yolov1
from yolo3d.dataset import load_label
from yolo3d.loss import iou, yolo_loss
from yolo3d.coordinates import minmax_to_xyzwhd, to_cell_repr, xyzwhd_to_minmax, from_cell_repr

def get_class_name(class_confidence):
    max_value = -1
    max_index = -1

    for index in range(len(class_confidence)):
        value = class_confidence[index]
        if value >= max_value:
            max_value = value
            max_index = index
        
    classes = ["tree", "rock"]

    return classes[max_index]

def get_class_color(pred_class):
    if pred_class == "tree":
        return 226
    if pred_class == "rock":
        return 236
    return 216

def by_trust(predicted_box):
    (trust, *_) = predicted_box
    return trust

def trust_greater_threshold(predicted_box):
    (trust, *_) = predicted_box
    return trust > 0.5

def get_predicted_boxes(y):
    predicted_boxes = []

    for i in range(y.shape[1]):
        for j in range(y.shape[2]):
            for k in range(y.shape[3]):
                prediction = y[0, i, j, k]
                trust = prediction[8]
                pred_class = get_class_name(prediction[0:2])
                xyz, whd = from_cell_repr(np.array([i,j,k]), prediction[2:5], prediction[5:8], 7, 28)
                p0, p1 = xyzwhd_to_minmax(xyz, whd)
                predicted_boxes.append((trust, pred_class, p0, p1))

    return predicted_boxes

def extract_most_relevant_boxes(predicted_boxes):
    predicted_boxes = list(filter(trust_greater_threshold, predicted_boxes))
    predicted_boxes.sort(key=by_trust, reverse=True)

    final_boxes = []
    for (new_trust, new_class, new_p0, new_p1) in predicted_boxes:
        should_add = True
        for (final_trust, final_class, final_p0, final_p1) in final_boxes:
            if new_class == final_class and iou(new_p0, new_p1, final_p0, final_p1) > 0.5:
                should_add = False
        
        if should_add:
            final_boxes.append((new_trust, new_class, new_p0, new_p1))
    return predicted_boxes

def print_boxes(boxes):
    for (trust, y_class, p0, p1) in boxes:
         xyz, whd = minmax_to_xyzwhd(p0, p1)
         cell_index, cell_offset, cell_whd = to_cell_repr(xyz, whd, 7, 28)
         print(f"  - {trust:.1f} {y_class} @ ({p0[0]:.1f} {p0[1]:.1f} {p0[2]:.1f}), ({p1[0]:.1f} {p1[1]:.1f} {p1[2]:.1f})")
        #  print(f"  - {trust:.1f} {y_class} @ {cell_index} : {cell_offset} : {cell_whd}")
        #  print(f"               ({xyz[0]:.1f} {xyz[1]:.1f} {xyz[2]:.1f}), ({whd[0]:.1f} {whd[1]:.1f} {whd[2]:.1f})")

def get_expected_boxes(y):
    expected_boxes = []

    for i in range(y.shape[0]):
        for j in range(y.shape[1]):
            for k in range(y.shape[2]):
                response = y[i, j, k]
                if response[8] > 0.5:
                    y_class = get_class_name(response[0:2])
                    xyz, whd = from_cell_repr(np.array([i,j,k]), response[2:5], response[5:8], 7, 28)
                    p0, p1 = xyzwhd_to_minmax(xyz, whd)
                    expected_boxes.append((1.0, y_class, p0, p1))

    return expected_boxes

def save_boxes_to_file(sample_name, vox_file, predicted_boxes):
    for (_, pred_class, p0, p1) in predicted_boxes:
        c = get_class_color(pred_class)
        p0 = K.cast(K.round(p0), "int32")
        p1 = K.cast(K.round(p1), "int32")

        for x in range(p0[0], p1[0]):
            vox_file.models[0].voxels.append(Voxel(x,p0[1],p0[2],c))
            vox_file.models[0].voxels.append(Voxel(x,p0[1],p1[2]-1,c))
            vox_file.models[0].voxels.append(Voxel(x,p1[1]-1,p0[2],c))
            vox_file.models[0].voxels.append(Voxel(x,p1[1]-1,p1[2]-1,c))

        for y in range(p0[1] + 1, p1[1] - 1):
            vox_file.models[0].voxels.append(Voxel(p0[0],y,p0[2],c))
            vox_file.models[0].voxels.append(Voxel(p0[0],y,p1[2]-1,c))
            vox_file.models[0].voxels.append(Voxel(p1[0]-1,y,p0[2],c))
            vox_file.models[0].voxels.append(Voxel(p1[0]-1,y,p1[2]-1,c))
        
        for z in range(p0[2] + 1, p1[2] - 1):
            vox_file.models[0].voxels.append(Voxel(p0[0],p0[1], z, c))
            vox_file.models[0].voxels.append(Voxel(p0[0],p1[1]-1, z, c))
            vox_file.models[0].voxels.append(Voxel(p1[0]-1,p0[1], z, c))
            vox_file.models[0].voxels.append(Voxel(p1[0]-1,p1[1]-1, z, c))

    VoxWriter(f"{sample_name}_prediction.vox", vox_file).write()

if __name__ == '__main__': 
    # sample_name = sys.argv[1]
    sample_name = "val_0"

    # setup model
    input_shape = (1, 28, 28, 28, 3)
    inputs = Input(input_shape[1:5])
    outputs = model_tiny_yolov1(inputs, 2)
    model = Model(inputs=inputs, outputs=outputs)
    model.load_weights('final-weights.hdf5', by_name=True)

    # load data
    vox_file = VoxParser(f"../dataset-3d-minecraft/{sample_name}.vox").parse()
    vox_rgb = vox_file.to_dense_rgb()
    vox_rgb = vox_rgb / 255.
    vox_rgb = np.reshape(vox_rgb, input_shape)
    
    for i in range(vox_rgb.shape[0]):
        for j in range(vox_rgb.shape[1]):
            for k in range(vox_rgb.shape[2]):
                [r,g,b] = vox_rgb[0,i,j,k]
                if r+g+b != 0:
                    print(f"[{i},{j},{k}]: ({r}, {g}, {b})")


    print(f"Predicting objects in: {sample_name}")

    # predict
    y = model.predict(vox_rgb, batch_size=1)
    y = K.variable(y)

    # compare with label
    y_true = load_label(f"../dataset-3d-minecraft/{sample_name}.txt", 28)
    y_true = K.variable(y_true)
    
    loss = yolo_loss(y_true, y)
    
    expected_boxes = get_expected_boxes(y_true)

    predicted_boxes = get_predicted_boxes(y)
    predicted_boxes = extract_most_relevant_boxes(predicted_boxes)

    print("Expected boxes for this sample:")
    print_boxes(expected_boxes)
    print("Predicted boxes for this sample:")
    print_boxes(predicted_boxes)
    print(f"Loss for this sample is {loss}")

    save_boxes_to_file(sample_name, vox_file, predicted_boxes)




    # Here is one of the possible non-maximal suppression implementation:

    # Sort the predictions by the confidence scores.
    # Start from the top scores, ignore any current prediction if we find any previous predictions that have the same class and IoU > 0.5 with the current prediction.
    # Repeat step 2 until all predictions are checked.