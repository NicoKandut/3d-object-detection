package nicok.bac.yolo3d.scanner;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.common.ScanResult;
import nicok.bac.yolo3d.inputfile.InputFile;
import nicok.bac.yolo3d.network.Network;

import java.util.ArrayList;

public record Scanner(Network network) {

    public ScanResult scan(final InputFile inputFile) { // Maybe move network to parameters here
        final var extent = inputFile.getBoundingBox();
        final var kernelSize = network.getExtent();
        final var objects = new ArrayList<ResultBoundingBox>();

        System.out.printf("File size %s\n", extent.size());

        int nrScans = 0;

        for (var z = extent.min().z(); z < extent.max().z(); z += kernelSize.z()) {
            for (var y = extent.min().y(); y < extent.max().y(); y += kernelSize.y()) {
                for (var x = extent.min().x(); x < extent.max().x(); x += kernelSize.x()) {
                    final var start = new Point(x, y, z);
                    final var box = new BoundingBox(start, Point.add(start, kernelSize));
                    final var volume = inputFile.read(box);

                    final var boundingBoxes = network.compute(box, volume);

                    objects.addAll(boundingBoxes);
                    nrScans++;
                }
            }
        }

        System.out.printf("Number of scans %d\n", nrScans);

        System.out.printf("Got %d bounding boxes to evaluate\n", objects.size());

        return new ScanResult(objects);
    }
}
