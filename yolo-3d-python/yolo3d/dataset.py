from keras.utils import Sequence
import math
from pyvox.parser import VoxParser
import numpy as np
import os

from .coordinates import minmax_to_xyzwhd, to_cell_repr

def load_label(label_path, vox_model_size=112, cell_count=7, num_classes=48):
    label_matrix = np.zeros([cell_count, cell_count, cell_count, num_classes + 6 + 1])
    with open(label_path, "r") as label_file:
        for label in label_file.readlines():
            parts = label.split()
            category = int(parts[0])

            xyz_min = np.array(parts[1:4]).astype(np.float32)
            xyz_max = np.array(parts[4:7]).astype(np.float32)

            xyz_center, whd = minmax_to_xyzwhd(xyz_min, xyz_max)
            cell_index, cell_offset, whd = to_cell_repr(xyz_center, whd, cell_count, vox_model_size)
            
            cell_x, cell_y, cell_z = cell_index
            offset_x, offset_y, offset_z = cell_offset
            w, h, d = whd
 
            if label_matrix[cell_x, cell_y, cell_z, -1] == 0:
                label_matrix[cell_x, cell_y, cell_z, category] = 1
                label_matrix[cell_x, cell_y, cell_z, num_classes:(num_classes + 6)] = [offset_x, offset_y, offset_z, w, h, d]
                label_matrix[cell_x, cell_y, cell_z, -1] = 1

    return label_matrix

class SequenceData(Sequence):

    def __init__(self, model, dir, batch_size, shuffle=True):
        self.model = model
        self.samples = []
        if self.model == 'train':
            with open(os.path.join(dir, 'train.txt'), 'r') as f:
                self.samples = self.samples + f.readlines()
        elif self.model == 'val':
            with open(os.path.join(dir, 'val.txt'), 'r') as f:
                self.samples = self.samples + f.readlines()
        self.batch_size = batch_size
        self.indexes = np.arange(len(self.samples))
        self.shuffle = shuffle
        self.dir = dir

    def __len__(self):
        num_samples = len(self.samples)
        return math.ceil(num_samples / float(self.batch_size))

    def __getitem__(self, idx):
        batch_indexs = self.indexes[idx * self.batch_size:(idx + 1) * self.batch_size]
        batch = [self.samples[k] for k in batch_indexs]
        X, y = self.data_generation(batch)

        return X, y

    def on_epoch_end(self):
        if self.shuffle:
            np.random.shuffle(self.indexes)

    def read(self, dataset):
        dataset = dataset.strip().split()
        image_path = dataset[0]
        label_path = dataset[1]

        vox_model = VoxParser(os.path.join(self.dir, image_path)).parse()
        vox_rgb_abs = vox_model.to_dense_alpha()
        vox_rgb_rel = vox_rgb_abs / 255.

        label_matrix = load_label(os.path.join(self.dir, label_path))
        
        # print(f"READING: {image_path}, {vox_rgb_rel.shape} voxels")

        return vox_rgb_rel, label_matrix

    def data_generation(self, batch_datasets):
        vox_models = []
        labels = []

        for dataset in batch_datasets:
            vox_model, label = self.read(dataset)
            vox_models.append(vox_model)
            labels.append(label)

        X = np.array(vox_models)
        y = np.array(labels)

        return X, y


