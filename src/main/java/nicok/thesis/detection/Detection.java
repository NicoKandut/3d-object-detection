package nicok.thesis.detection;

import nicok.thesis.config.Constants;
import nicok.thesis.performance.Timer;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

import static nicok.thesis.config.Constants.RESOURCE_DIR;
import static org.opencv.dnn.Dnn.*;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.rectangle;

public class Detection {
    /**
     * Source: <a href="https://github.com/pjreddie/darknet/blob/master/cfg/yolov3.cfg">On Github</a>
     */
    private static final String modelConfiguration = RESOURCE_DIR + "/yolov3.cfg";

    /**
     * Source: <a href="https://pjreddie.com/media/files/yolov3.weights">Weights (237 MB)</a>
     */
    private static final String modelWeights = RESOURCE_DIR + "/yolov3.weights";

    public static Timer run(String imagePath, String outputPath) {
        final var timer = new Timer();

        timer.addMark("start");

        // load model with weights and config
        timer.addMark("model-start");
        final var net = readNetFromDarknet(modelConfiguration, modelWeights);
        timer.addMark("model-end");

        // convert image to blob, the blob can then be used as input for the model
        timer.addMark("blob-start");
        final var frame = imread(imagePath);
        final var sz = new Size(288, 288);
        final var blob = blobFromImage(frame, 0.00392, sz, new Scalar(0), true, false);
        timer.addMark("blob-end");

        //Feed forward the model to get output
        timer.addMark("forward-start");
        final var result = new ArrayList<Mat>();
        final var outBlobNames = getOutputNames(net);
        net.setInput(blob);
        net.forward(result, outBlobNames);
        timer.addMark("forward-end");

        // work through the outputs of the model and analyze them
        timer.addMark("outputs-start");
        float confThreshold = 0.6f; // Insert thresholding beyond which the model will detect objects
        final var clsIds = new ArrayList<Integer>();
        final var confs = new ArrayList<Float>();
        final var rects = new ArrayList<Rect2d>();
        for (final var level : result) {
            // each row is a candidate detection, the 1st 4 numbers are
            // [center_x, center_y, width, height], followed by (N-4) class probabilities
            for (int j = 0; j < level.rows(); ++j) {
                Mat row = level.row(j);
                Mat scores = row.colRange(5, level.cols());
                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                float confidence = (float) mm.maxVal;
                Point classIdPoint = mm.maxLoc;
                if (confidence > confThreshold) {
                    int centerX = (int) (row.get(0, 0)[0] * frame.cols()); //scaling for drawing the bounding boxes//
                    int centerY = (int) (row.get(0, 1)[0] * frame.rows());
                    int width = (int) (row.get(0, 2)[0] * frame.cols());
                    int height = (int) (row.get(0, 3)[0] * frame.rows());
                    int left = centerX - width / 2;
                    int top = centerY - height / 2;

                    clsIds.add((int) classIdPoint.x);
                    confs.add(confidence);
                    rects.add(new Rect2d(left, top, width, height));
                }
            }
        }
        timer.addMark("outputs-end");

        timer.addMark("boxes-start");
        final var nmsThresh = 0.5f;
        final var confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
        final var boxesArray = rects.toArray(new Rect2d[0]);
        final var boxes = new MatOfRect2d(boxesArray);
        final var indices = new MatOfInt();
        NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices); //We draw the bounding boxes for objects here//
        timer.addMark("boxes-end");

        timer.addMark("draw-start");
        for (final var idx : indices.toArray()) {
            final var box = boxesArray[idx];
            rectangle(frame, box.tl(), box.br(), new Scalar(0, 0, 255), 2);
        }
        timer.addMark("draw-end");

        timer.addMark("save-start");
        imwrite(outputPath, frame);
        timer.addMark("save-end");
        timer.addMark("end");

        return timer;
    }

    private static List<String> getOutputNames(Net net) {
        List<String> names = new ArrayList<>();

        List<Integer> outLayers = net.getUnconnectedOutLayers().toList();
        List<String> layersNames = net.getLayerNames();

        outLayers.forEach((item) -> names.add(layersNames.get(item - 1)));//unfold and create R-CNN layers from the loaded YOLO model//
        return names;
    }

}
