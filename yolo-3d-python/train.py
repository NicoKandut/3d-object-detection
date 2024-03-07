import os
import argparse

from keras.layers import Input
from keras.models import Model
from keras.callbacks import ModelCheckpoint, EarlyStopping

from yolo3d.dataset import SequenceData
from yolo3d.model import input_shape, model_tiny_yolov1
from yolo3d.loss import yolo_loss

if __name__ == '__main__': 
    parser=argparse.ArgumentParser()
    parser.add_argument("--epochs", help="Number of epochs to train")
    args=parser.parse_args()
    epochs = int(args.epochs)

    inputs = Input(input_shape())
    outputs = model_tiny_yolov1(inputs, pooling_layers=5)

    model = Model(inputs=inputs, outputs=outputs)
    model.compile(loss=yolo_loss, optimizer='adam')

    model.summary()

    # save_dir = 'checkpoints'
    # weights_path = os.path.join(save_dir, 'weights.hdf5')
    # checkpoint = ModelCheckpoint(weights_path, monitor='val_loss', save_weights_only=True, save_best_only=True)

    # if not os.path.isdir(save_dir):
    #     os.makedirs(save_dir)

    # if os.path.exists('checkpoints/weights.hdf5'):
    #     model.load_weights('checkpoints/weights.hdf5', by_name=True)
      
    early_stopping = EarlyStopping(monitor='val_loss', min_delta=0, patience=15, verbose=1, mode='auto')

    datasets_path = "../dataset-psb-vox"
    batch_size = 100

    train_generator = SequenceData('train', datasets_path, batch_size)
    validation_generator = SequenceData('val', datasets_path, batch_size)

    model.fit(
        train_generator,
        steps_per_epoch=len(train_generator),
        epochs=epochs,
        validation_data=validation_generator,
        validation_steps=len(validation_generator),
        # use_multiprocessing=True,
        workers=4,
        callbacks=[
            # checkpoint,
            early_stopping
        ]
    )

    model.save_weights('final-weights.hdf5')
    model.save("saved_model")

    