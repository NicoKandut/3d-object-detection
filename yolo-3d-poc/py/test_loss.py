import unittest
import tensorflow as tf
import keras.backend as K

from yolo3d.loss import xyzwhd2minmax, yolo_box_loss, yolo_class_loss, yolo_confidence_loss, yolo_loss, iou

class TestLoss(unittest.TestCase):
    def test_loss_zero_iou(self):
        shape = (2,7,7,7,3)
        xyz = tf.random.uniform(shape, minval=0.0, maxval=1.0)
        whd = tf.random.uniform(shape, minval=0.0, maxval=1.0)
        (xyz_min, xyz_max) = xyzwhd2minmax(xyz, whd)

        expected = tf.ones((2,7,7,7,1))
        result = iou(xyz_min, xyz_max, xyz_min, xyz_max)

        tf.debugging.assert_equal(expected, result)

    def test_loss_zero_overall_with_ones(self):
        shape = (2,7,7,7,9)
        y = tf.ones(shape)

        expected = tf.zeros((1,1))
        result = yolo_loss(y, y)

        tf.debugging.assert_equal(expected, result)

    def test_loss_zero_overall_with_zeros(self):
        shape = (2,7,7,7,9)
        y = tf.zeros(shape)

        expected = tf.zeros((1,1))
        result = yolo_loss(y, y)

        tf.debugging.assert_equal(expected, result)

    def test_class_loss_slope(self):
        y_true = tf.constant([1,0,1,1,1,6,6,6,1], dtype=float, shape=(1,1,1,1,9))

        y_pred_0 = tf.constant([1,0,1,1,1,6,6,6,1], dtype=float, shape=(1,1,1,1,9))
        loss_0 = K.sum(yolo_class_loss(y_true, y_pred_0))
        self.assertEqual(0, loss_0)

        y_pred_1 = tf.constant([0.8,0.2,1,1,1,6,6,6,1], dtype=float, shape=(1,1,1,1,9))
        loss_1 = K.sum(yolo_class_loss(y_true, y_pred_1))
        self.assertGreater(loss_1, loss_0)

        y_pred_2 = tf.constant([0.5,0.5,1,1,1,6,6,6,1], dtype=float, shape=(1,1,1,1,9))
        loss_2 = K.sum(yolo_class_loss(y_true, y_pred_2))
        self.assertGreater(loss_2, loss_1)

        y_pred_3 = tf.constant([0,1,1,1,1,6,6,6,1], dtype=float, shape=(1,1,1,1,9))
        loss_3 = K.sum(yolo_class_loss(y_true, y_pred_3))
        self.assertGreater(loss_3, loss_2)

    def test_class_loss_mask(self):
        y_true = tf.zeros(dtype=float, shape=(1,1,1,1,9))
        y_pred = tf.constant([1,0,1,1,1,6,6,6,1], dtype=float, shape=(1,1,1,1,9))
        loss = K.sum(yolo_class_loss(y_true, y_pred))
        self.assertEqual(0, loss)


    def test_box_loss_slope(self):
        y_true = tf.constant([1,0,1,1,1,6,6,6,1], dtype=float, shape=(1,1,1,1,9))

        y_pred_0 = tf.constant([1,0,1,1,1,6,6,6,1], dtype=float, shape=(1,1,1,1,9))
        loss_0 = K.sum(yolo_box_loss(y_true, y_pred_0))
        self.assertEqual(0, loss_0)

        y_pred_1 = tf.constant([1,0,1,1,1,6,6,5,1], dtype=float, shape=(1,1,1,1,9))
        loss_1 = K.sum(yolo_box_loss(y_true, y_pred_1))
        self.assertGreater(loss_1, loss_0)

        y_pred_2 = tf.constant([1,0,1,2,1,6,6,5,1], dtype=float, shape=(1,1,1,1,9))
        loss_2 = K.sum(yolo_box_loss(y_true, y_pred_2))
        self.assertGreater(loss_2, loss_1)

        y_pred_3 = tf.constant([1,0,1,2,1,6,8,5,1], dtype=float, shape=(1,1,1,1,9))
        loss_3 = K.sum(yolo_box_loss(y_true, y_pred_3))
        self.assertGreater(loss_3, loss_2)

    def test_box_loss_mask(self):
        y_true = tf.zeros(dtype=float, shape=(1,1,1,1,9))
        y_pred = tf.constant([1,0,1,1,1,6,6,6,1], dtype=float, shape=(1,1,1,1,9))
        loss = K.sum(yolo_box_loss(y_true, y_pred))
        self.assertEqual(0, loss)


    def test_confidence_loss(self):
        y_true = tf.constant([1,0,1,1,1,6,6,6,1], dtype=float, shape=(1,1,1,1,9))
        y_pred = tf.constant([1,0,1,1,1,6,6,6,1], dtype=float, shape=(1,1,1,1,9))
        loss = K.sum(yolo_confidence_loss(y_true, y_pred))
        self.assertEqual(0, loss) 

        y_true = tf.constant([0,0,0,0,0,0,0,0,0], dtype=float, shape=(1,1,1,1,9))
        y_pred = tf.constant([1,0,1,1,1,6,6,6,0], dtype=float, shape=(1,1,1,1,9))
        loss = K.sum(yolo_confidence_loss(y_true, y_pred))
        self.assertEqual(0, loss) 

        y_true = tf.constant([0,0,0,0,0,0,0,0,0], dtype=float, shape=(1,1,1,1,9))
        y_pred = tf.constant([0,0,0,0,0,0,0,0,1], dtype=float, shape=(1,1,1,1,9))
        loss = K.sum(yolo_confidence_loss(y_true, y_pred))
        self.assertGreater(loss, 0) 

        y_true = tf.constant([1,0,1,1,1,6,6,6,1], dtype=float, shape=(1,1,1,1,9))
        y_pred = tf.constant([1,0,1,1,1,6,6,6,0], dtype=float, shape=(1,1,1,1,9))
        loss = K.sum(yolo_confidence_loss(y_true, y_pred))
        self.assertGreater(loss, 0) 


if __name__ == "__main__":
    unittest.main()