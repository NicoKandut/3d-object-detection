import sys
import keras.backend as K
import numpy as np

from keras.layers import Input
from keras.models import Model

from pyvox.parser import VoxParser
from pyvox.writer import VoxWriter
from pyvox.models import Model as VoxModel, Voxel

from yolo3d.model import model_tiny_yolov1
from yolo3d.dataset import load_label
from yolo3d.loss import yolo_loss
from yolo3d.coordinates import xyzwhd_to_minmax, from_cell_repr

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

if __name__ == '__main__': 
    sample_name = sys.argv[1]

    # setup model
    input_shape = (1, 28, 28, 28, 3)
    inputs = Input(input_shape[1:5])
    outputs = model_tiny_yolov1(inputs, 2)
    model = Model(inputs=inputs, outputs=outputs)
    model.load_weights('final-weights.hdf5', by_name=True)

    # load data
    vox_file = VoxParser(f"../dataset-3d-minecraft/{sample_name}.vox").parse()
    vox_rgb = vox_file.to_dense_rgb() / 255.
    vox_rgb = np.reshape(vox_rgb, input_shape)
    print(f"Predicting objects in: {sample_name}")

    # predict
    y = model.predict(vox_rgb, batch_size=1)
    y = K.variable(y)

    y_true = load_label(f"../dataset-3d-minecraft/{sample_name}.txt", 28)
    y_true = K.variable(y_true)

    loss = yolo_loss(y_true, y)
    print(f"Loss for this sample is {loss}")

    print("EXPECTED VALUES FOR THIS SAMPLE:")
    for i in range(y_true.shape[0]):
        for j in range(y_true.shape[1]):
            for k in range(y_true.shape[2]):
                response = y_true[i, j, k]
                if response[8] > 0.5:
                    y_class = get_class_name(response[0:2])
                    xyz, whd = from_cell_repr(np.array([i,j,k]), response[2:5], response[5:8], 7, 28)
                    p0, p1 = xyzwhd_to_minmax(xyz, whd)
                    print(f"  - 1.0 {y_class} @ ({p0[0]:.1f} {p0[1]:.1f} {p0[2]:.1f}), ({p1[0]:.1f} {p1[1]:.1f} {p1[2]:.1f})")

    predicted_boxes = []

    print("Predicted objects in this sample:")
    for i in range(y.shape[1]):
        for j in range(y.shape[2]):
            for k in range(y.shape[3]):
                prediction = y[0, i, j, k]
                trust = prediction[8]
                if trust > 0.4:
                    pred_class = get_class_name(prediction[0:2])
                    xyz, whd = from_cell_repr(np.array([i,j,k]), prediction[2:5], prediction[5:8], 7, 28)
                    p0, p1 = xyzwhd_to_minmax(xyz, whd)
                    print(f"  - {trust:.1f} {pred_class} @ ({p0[0]:.1f} {p0[1]:.1f} {p0[2]:.1f}), ({p1[0]:.1f} {p1[1]:.1f} {p1[2]:.1f})")
                    predicted_boxes.append((pred_class, p0, p1))
    
    for (pred_class, p0, p1) in predicted_boxes:
        c = get_class_color(pred_class)

        p0 = K.cast(K.round(p0), "int32")
        p1 = K.cast(K.round(p1), "int32")

        for x in range(p0[0], p1[0]):
            vox_file.models[0].voxels.append(Voxel(x,p0[1],p0[2],c))
            vox_file.models[0].voxels.append(Voxel(x,p0[1],p1[2]-1,c))
            vox_file.models[0].voxels.append(Voxel(x,p1[1]-1,p0[2],c))
            vox_file.models[0].voxels.append(Voxel(x,p1[1]-1,p1[2]-1,c))

        for y in range(p0[1], p1[1]):
            vox_file.models[0].voxels.append(Voxel(p0[0],y,p0[2],c))
            vox_file.models[0].voxels.append(Voxel(p0[0],y,p1[2]-1,c))
            vox_file.models[0].voxels.append(Voxel(p1[0]-1,y,p0[2],c))
            vox_file.models[0].voxels.append(Voxel(p1[0]-1,y,p1[2]-1,c))
        
        for z in range(p0[2], p1[2]):
            vox_file.models[0].voxels.append(Voxel(p0[0],p0[1], z, c))
            vox_file.models[0].voxels.append(Voxel(p0[0],p1[1]-1, z, c))
            vox_file.models[0].voxels.append(Voxel(p1[0]-1,p0[1], z, c))
            vox_file.models[0].voxels.append(Voxel(p1[0]-1,p1[1]-1, z, c))

    VoxWriter(f"{sample_name}_prediction.vox", vox_file).write()




    # Here is one of the possible non-maximal suppression implementation:

    # Sort the predictions by the confidence scores.
    # Start from the top scores, ignore any current prediction if we find any previous predictions that have the same class and IoU > 0.5 with the current prediction.
    # Repeat step 2 until all predictions are checked.