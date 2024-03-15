package nicok.bac.yolo3d.scanner;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.common.ScanResult;
import nicok.bac.yolo3d.inputfile.InputFile;
import nicok.bac.yolo3d.network.Network;
import nicok.bac.yolo3d.off.Vertex;

import java.util.ArrayList;

public record Scanner() {

    public ScanResult scan(
            final InputFile inputFile,
            final Network network
    ) {
        final var extent = inputFile.getBoundingBox();
        final var kernelSize = network.getExtent();
        final var objects = new ArrayList<ResultBoundingBox>();

        System.out.printf("File size %s\n", extent.size());

        final var scansWidth = Math.ceil(extent.size().x() / network.getExtent().x());
        final var scansHeight = Math.ceil(extent.size().y() / network.getExtent().y());
        final var scansDepth = Math.ceil(extent.size().z() / network.getExtent().z());

        final var estimatedScans = scansWidth * scansHeight * scansDepth;

        var nrScans = 0;

        for (var z = extent.min().z(); z < extent.max().z(); z += kernelSize.z()) {
            for (var y = extent.min().y(); y < extent.max().y(); y += kernelSize.y()) {
                for (var x = extent.min().x(); x < extent.max().x(); x += kernelSize.x()) {
                    final var start = new Vertex(x, y, z);
                    final var box = new BoundingBox(start, Vertex.add(start, kernelSize));
                    final var volume = inputFile.read(box);

                    final var boundingBoxes = network.compute(box, volume);

                    objects.addAll(boundingBoxes);
                    ++nrScans;

                    System.out.printf("Scan %d of %d: %.0f%%\n", nrScans, (int) estimatedScans, nrScans / estimatedScans * 100);
                }
            }
        }

//        System.out.printf("Number of scans %d\n", nrScans);

        System.out.printf("Got %d bounding boxes to evaluate\n", objects.size());

        return new ScanResult(objects);
    }
}
