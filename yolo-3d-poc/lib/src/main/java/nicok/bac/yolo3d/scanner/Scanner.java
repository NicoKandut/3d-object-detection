package nicok.bac.yolo3d.scanner;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.collection.PersistentResultBoundingBoxList;
import nicok.bac.yolo3d.common.ScanResult;
import nicok.bac.yolo3d.inputfile.InputFile;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.network.Network;
import nicok.bac.yolo3d.terminal.ProgressBar;
import nicok.bac.yolo3d.util.RepositoryPaths;

public record Scanner() {

    public ScanResult scan(final InputFile inputFile, final Network network) throws Exception {
        final var extent = inputFile.getBoundingBox();
        System.out.printf("File size %s\n", extent.size());

        final var kernelSize = network.size();
        final var stride = Vertex.div(kernelSize, 2);

        final var nrSteps = getNumberOfScans(extent, kernelSize, stride);
        final var nrScans = (long) (nrSteps.x() * nrSteps.y() * nrSteps.z());
        var currentScan = 1;

        System.out.println("Scanning file");
        final var progressBar = new ProgressBar(20, nrScans);
        final var boxWriter = PersistentResultBoundingBoxList.writer(RepositoryPaths.BOX_DATA_TEMP + "/scan.boxes");
        var nrBoxes = 0;
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

                    boundingBoxes.forEach(boxWriter::write);
                    progressBar.printProgress(currentScan);
                    nrBoxes += boundingBoxes.size();

                    ++currentScan;
                    x += stride.x();
                }
                y += stride.y();
            }
            z += stride.z();
        }

        boxWriter.close();

        final var boxes = boxWriter.getList();
        verifyNrBoxes(nrBoxes, boxes);

        System.out.printf("Got %d bounding boxes to evaluate\n", boxes.size());

        return new ScanResult(boxes);
    }

    private static void verifyNrBoxes(int nrBoxes, PersistentResultBoundingBoxList boxes) {
        if (nrBoxes != boxes.size()) {
            throw new IllegalStateException(String.format(
                    "Number of boxes written does not match expected number: %d vs %d",
                    nrBoxes,
                    boxes.size()
            ));
        }
    }

    private static Vertex getNumberOfScans(final BoundingBox extent, final Vertex kernelSize, final Vertex stride) {
        final var adjustedSize = Vertex.componentWiseMultiply(Vertex.ceil(Vertex.componentWiseDiv(extent.size(), stride)), stride);
        final var adjustedExtent = new BoundingBox(extent.min(), Vertex.add(extent.min(), adjustedSize));
        return Vertex.add(Vertex.componentWiseDiv(Vertex.sub(adjustedExtent.size(), kernelSize), stride), Vertex.ONE);
    }
}
