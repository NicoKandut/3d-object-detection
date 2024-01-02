import numpy as np

def xyzwhd_to_minmax(xyz, whd):
    xyz_min = xyz - whd / 2
    xyz_max = xyz + whd / 2

    return xyz_min, xyz_max

def minmax_to_xyzwhd(xyz_min, xyz_max):
    xyz_center = (xyz_min + xyz_max) / 2
    whd = (xyz_max - xyz_min)

    return xyz_center, whd

def to_cell_repr(xyz, whd, cell_count, full_size):
    xyz_cell = xyz * cell_count / full_size
    cell_index = np.floor(xyz_cell).astype(np.int32)
    cell_offset = xyz_cell - cell_index
    cell_whd = whd / full_size

    return cell_index, cell_offset, cell_whd

def from_cell_repr(cell_index, cell_offset, cell_whd, cell_count, full_size):
    xyz = (cell_index + cell_offset) * full_size / cell_count
    whd = cell_whd * full_size

    return xyz, whd
