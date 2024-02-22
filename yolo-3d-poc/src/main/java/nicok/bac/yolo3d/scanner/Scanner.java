package nicok.bac.yolo3d.scanner;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.common.ScanResult;
import nicok.bac.yolo3d.inputfile.InputFile;

import java.util.ArrayList;

public record Scanner(Network network) {

    public ScanResult scan(final InputFile inputFile) {
        final var extent = inputFile.getBoundingBox();
        final var kernelSize = network.getExtent();
        final var objects =  new ArrayList<ResultBoundingBox>();

        for (var z = extent.min().z(); z < extent.max().z(); z += kernelSize.z()) {
            for (var y = extent.min().y(); y < extent.max().y(); y += kernelSize.y()) {
                for (var x = extent.min().x(); x < extent.max().x(); x += kernelSize.x()) {
                    final var start = new Point(x, y, z);
                    final var box = new BoundingBox(start, Point.add(start, kernelSize));
                    final var volume = inputFile.read(box);
                    final var boundingBoxes = network.compute(volume);

                    objects.addAll(boundingBoxes);
                }
            }
        }

        return new ScanResult(objects);
    }
}
