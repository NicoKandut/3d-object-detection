import unittest
import numpy as np

from yolo3d.coordinates import from_cell_repr, to_cell_repr, xyzwhd_to_minmax, minmax_to_xyzwhd

class TestCoordinates(unittest.TestCase):
    def test_coordinate_transform_roundtrip(self):
        box_p0 = np.array([0,1,2])
        box_p1 = np.array([10,20,30])

        center, whd = minmax_to_xyzwhd(box_p0, box_p1)

        self.assertEqual(center[0], 5)
        self.assertEqual(center[1], 10.5)
        self.assertEqual(center[2], 16)
        self.assertEqual(whd[0], 10)
        self.assertEqual(whd[1], 19)
        self.assertEqual(whd[2], 28)

        p0,p1 = xyzwhd_to_minmax(center, whd)

        self.assertEqual(box_p0[0], p0[0])
        self.assertEqual(box_p0[1], p0[1])
        self.assertEqual(box_p0[2], p0[2])
        self.assertEqual(box_p1[0], p1[0])
        self.assertEqual(box_p1[1], p1[1])
        self.assertEqual(box_p1[2], p1[2])

    def test_cell_transformation(self):
        center = np.array([2,5,8])
        whd = np.array([2,4,1])

        cell_index, cell_offset, whd_cell = to_cell_repr(center, whd, 4, 10)

        self.assertAlmostEqual(cell_index[0], 0)
        self.assertAlmostEqual(cell_index[1], 2)
        self.assertAlmostEqual(cell_index[2], 3)
        self.assertAlmostEqual(cell_offset[0], 0.8)
        self.assertAlmostEqual(cell_offset[1], 0.0)
        self.assertAlmostEqual(cell_offset[2], 0.2)
        self.assertAlmostEqual(whd_cell[0], 0.2)
        self.assertAlmostEqual(whd_cell[1], 0.4)
        self.assertAlmostEqual(whd_cell[2], 0.1)

        center_after, whd_after = from_cell_repr(cell_index, cell_offset, whd_cell, 4, 10)

        self.assertEqual(center[0], center_after[0])
        self.assertEqual(center[1], center_after[1])
        self.assertEqual(center[2], center_after[2])
        self.assertEqual(whd[0], whd_after[0])
        self.assertEqual(whd[1], whd_after[1])
        self.assertEqual(whd[2], whd_after[2])


if __name__ == "__main__":
    unittest.main()