package nicok.bac.yolo3d.scanner;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.common.ScanResult;
import nicok.bac.yolo3d.inputfile.InputFile;
import nicok.bac.yolo3d.network.Network;
import nicok.bac.yolo3d.off.Vertex;
import nicok.bac.yolo3d.terminal.ProgressBar;

import java.util.ArrayList;

public record Scanner() {

    public ScanResult scan(
            final InputFile inputFile,
            final Network network
    ) {
        final var extent = inputFile.getBoundingBox();
        System.out.printf("File size %s\n", extent.size());

        final var kernelSize = network.getExtent();
        final var stride = Vertex.div(kernelSize, 2);

        final var nrSteps = getNumberOfScans(extent, kernelSize, stride);
        final var nrScans = (long) (nrSteps.x() * nrSteps.y() * nrSteps.z());
        var currentScan = 1;

        System.out.println("Scanning file");
        final var progressBar = new ProgressBar(20, nrScans);
        final var objects = new ArrayList<ResultBoundingBox>();

        var x = extent.min().x();
        var y = extent.min().y();
        var z = extent.min().z();
        for (var indexZ = 0; indexZ < nrSteps.z(); ++indexZ) {
            for (var indexY = 0; indexY < nrSteps.y(); ++indexY) {
                for (var indexX = 0; indexX < nrSteps.x(); ++indexX) {
                    final var start = new Vertex(x, y, z);
                    final var end = Vertex.add(start, kernelSize);
                    final var box = new BoundingBox(start, end);
                    final var volume = inputFile.read(box);
                    final var boundingBoxes = network.compute(box, volume);

                    objects.addAll(boundingBoxes);

                    progressBar.printProgress(currentScan);
                    ++currentScan;
                    x += stride.x();
                    y += stride.y();
                    z += stride.z();
                }
            }
        }

        System.out.printf("Got %d bounding boxes to evaluate\n", objects.size());

        return new ScanResult(objects);
    }

    private Vertex getNumberOfScans(
            final BoundingBox extent,
            final Vertex kernelSize,
            final Vertex stride
    ) {
        final var adjustedSize = Vertex.componentWiseMultiply(Vertex.ceil(Vertex.componentWiseDiv(extent.size(), stride)), stride);
        final var adjustedExtent = new BoundingBox(extent.min(), Vertex.add(extent.min(), adjustedSize));

        final var x = (adjustedExtent.size().x() - kernelSize.x()) / stride.x() + 1;
        final var y = (adjustedExtent.size().y() - kernelSize.y()) / stride.y() + 1;
        final var z = (adjustedExtent.size().z() - kernelSize.z()) / stride.z() + 1;

        return new Vertex(x, y, z);
    }
}
