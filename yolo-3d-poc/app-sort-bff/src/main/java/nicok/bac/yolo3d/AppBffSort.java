package nicok.bac.yolo3d;

import nicok.bac.yolo3d.collection.CustomCollectors;
import nicok.bac.yolo3d.collection.PersistentLongList;
import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.storage.bff.BffReaderRAF;
import nicok.bac.yolo3d.storage.bff.BffWriter;
import nicok.bac.yolo3d.terminal.ProgressBar;
import nicok.bac.yolo3d.util.RepositoryPaths;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.util.Comparator;
import java.util.stream.LongStream;

import static java.util.Comparator.comparingDouble;
import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.terminal.CommandLineUtil.parseCommandLine;
import static nicok.bac.yolo3d.util.DirectoryUtil.*;
import static nicok.bac.yolo3d.util.ExceptionUtil.unchecked;

public class AppBffSort {

    public static final Options OPTIONS = new Options()
            .addRequiredOption("i", "input", true, "Input file.")
            .addOption("h", "help", false, "Display this help message");

    /**
     * Usage: :app-mess-up-bff-file:run --args='-i C:/src/bac/dataset-psb/db/0/m5/m5.bff'
     */
    public static void main(final String[] args) throws Exception {
        final var commandLine = parseCommandLine("AppBffSort", args, OPTIONS);
        final var inputPath = commandLine.getOptionValue("input");

        requireNonNull(inputPath);
        requireExtension(inputPath, ".bff");

        try (final var bffReader = new BffReaderRAF(inputPath, 10000, 1000)) {

            final var vertexComparator = comparingDouble((final Long a) -> bffReader.getVertex(a).z());
            final var faceComparator = comparingDouble((final Long a) -> bffReader.getFace(a)
                    .vertexIndices()
                    .stream()
                    .map(bffReader::getVertex)
                    .mapToDouble(Vertex::z)
                    .min()
                    .orElseThrow());

            final var header = bffReader.header();
            System.out.println(header);
            final var vertexCount = header.vertexCount();
            final var faceCount = header.faceCount();

            // batch size could be a lot bigger
//            final var memoryDefinedBatchSize = (Runtime.getRuntime().freeMemory() / 2) / 8;
            final var memoryDefinedBatchSize = 20000;
            final var vertexBatchSize = (int) Math.min(Math.min(memoryDefinedBatchSize, vertexCount), Integer.MAX_VALUE);
            final var faceBatchSize = (int) Math.min(Math.min(memoryDefinedBatchSize, faceCount), Integer.MAX_VALUE);

            final var outputDir = RepositoryPaths.SORTING_TEMP + "/" + getFilename(inputPath);
            cleanDirectory(outputDir);

            System.out.println("Sorting vertices in batches");
            final var vertexProgressBar = new ProgressBar(20, vertexCount);
            final var numberOfVertexBatches = (long) Math.ceil((double) vertexCount / (double) vertexBatchSize);
            if (numberOfVertexBatches * vertexBatchSize < vertexCount) {
                throw new IllegalStateException("Insufficient number of batches for vertices");
            }
            final var sortedVertexBatches = LongStream.range(0, numberOfVertexBatches)
                    .mapToObj(i -> sortVertexBatch(i, outputDir, vertexBatchSize, vertexCount, vertexComparator, vertexProgressBar))
                    .toList();

            System.out.println("Sorting faces in batches");
            final var faceProgressBar = new ProgressBar(20, faceCount);
            final var numberOfFaceBatches = (long) Math.ceil((double) faceCount / (double) faceBatchSize);
            if (numberOfFaceBatches * faceBatchSize < faceCount) {
                throw new IllegalStateException("Insufficient number of batches for faces");
            }
            final var sortedFaceBatches = LongStream.range(0, numberOfVertexBatches)
                    .mapToObj(i -> sortFaceBatch(i, outputDir, faceBatchSize, faceCount, faceComparator, faceProgressBar))
                    .toList();

            System.out.println("Merging vertices");
            final var vertexFile = outputDir + "/complete.v.idx";
            final var sortedVertices = PersistentLongList.mergeSorted(vertexFile, sortedVertexBatches, vertexComparator);

            System.out.println("Merging faces");
            final var faceFile = outputDir + "/complete.f.idx";
            final var sortedFaces = PersistentLongList.mergeSorted(faceFile, sortedFaceBatches, faceComparator);

            System.out.println("Writing sorted file");
            final var sortedBffPath = inputPath.replace(".bff", ".sorted.bff");
            writeSortedFile(sortedBffPath, bffReader, sortedVertices, sortedFaces);
            verifyOrder(sortedBffPath);

            bffReader.printStatistic();
        }

        System.out.println("DONE");
    }

    private static PersistentLongList sortVertexBatch(long i, String outputDir, int vertexBatchSize, long vertexCount, Comparator<Long> vertexComparator, ProgressBar vertexProgressBar) {
        final var path = String.format("%s/batch_%d.v.idx", outputDir, i);
        final var batchStart = i * vertexBatchSize;
        final var batchEnd = Math.min(vertexCount, batchStart + vertexBatchSize);

        final var newVertexOrder = LongStream.range(batchStart, batchEnd)
                .boxed()
                .sorted(vertexComparator)
                .mapToLong(Long::longValue);

        try {
            return PersistentLongList.from(path, newVertexOrder, batchEnd - batchStart);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            vertexProgressBar.printProgress(batchEnd);
        }
    }

    private static PersistentLongList sortFaceBatch(long i, String outputDir, int faceBatchSize, long faceCount, Comparator<Long> faceComparator, ProgressBar faceProgressBar) {
        final var path = String.format("%s/batch_%d.f.idx", outputDir, i);
        final var batchStart = i * faceBatchSize;
        final var batchEnd = Math.min(faceCount, batchStart + faceBatchSize);

        return LongStream.range(batchStart, batchEnd)
                .boxed()
                .sorted(faceComparator)
                .onClose(() -> faceProgressBar.printProgress(batchEnd))
                .collect(CustomCollectors.toPersistentLongList(path));
    }

    private static void writeSortedFile(
            final String sortedBffPath,
            final BffReaderRAF bffReader,
            final PersistentLongList vertices,
            final PersistentLongList faces
    ) throws Exception {
        final var indexMappingPath = sortedBffPath + ".v.map";
        final var precisionBytes = bffReader.header().precisionBytes();
        final var indexBytes = bffReader.header().indexBytes();

        try (
                final var bffWriter = new BffWriter(sortedBffPath, precisionBytes, indexBytes);
                final var indexMapping = new PersistentLongList(indexMappingPath, vertices.size())
        ) {
            System.out.println("Writing vertices");
            final var vertexProgressBar = new ProgressBar(20, vertices.size());
            LongStream.range(0, vertices.size())
                    .forEach(unchecked((long newVertexIndex) -> {
                        final var oldVertexIndex = vertices.get(newVertexIndex);
                        final var vertex = bffReader.getVertex(oldVertexIndex);
                        bffWriter.writeVertex(vertex);
                        indexMapping.set(oldVertexIndex, newVertexIndex);
                        if (newVertexIndex % 1000 == 0 || newVertexIndex == vertices.size() - 1) {
                            vertexProgressBar.printProgress(newVertexIndex + 1);
                        }
                    }));

            System.out.println("Writing faces");
            final var faceProgressBar = new ProgressBar(20, faces.size());
            LongStream.range(0, faces.size())
                    .forEach(unchecked((long newFaceIndex) -> {
                        final var oldFaceIndex = faces.getItem(newFaceIndex);
                        final var oldFace = bffReader.getFace(oldFaceIndex);
                        final var newVertexIndices = oldFace.vertexIndices().stream()
                                .map(unchecked((Long index) -> indexMapping.get(index)))
                                .toList();
                        final var newFace = new Face(newVertexIndices);
                        bffWriter.writeFace(newFace);
                        if (newFaceIndex % 1000 == 0 || newFaceIndex == faces.size() - 1) {
                            faceProgressBar.printProgress(newFaceIndex + 1);
                        }
                    }));

            // write header
            bffWriter.writeHeader();
        }
    }

    private static void verifyOrder(String sortedBffPath) throws Exception {
        System.out.println("Verifying vertices");
        try (final var bffReader = new BffReaderRAF(sortedBffPath, 10000, 1000)) {
            final var vertexCount = bffReader.header().vertexCount();
            final var faceCount = bffReader.header().faceCount();

            var previousZ = Double.NEGATIVE_INFINITY;
            for (long vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
                final var vertex = bffReader.readVertex(vertexIndex);
                final var currentZ = vertex.z();

                if (currentZ < previousZ) {
                    throw new IllegalStateException("Vertices are not sorted, + Index: " + vertexIndex);
                }

                previousZ = currentZ;
            }

            previousZ = Double.NEGATIVE_INFINITY;
            for (long faceIndex = 0; faceIndex < faceCount; faceIndex++) {
                final var face = bffReader.getFace(faceIndex);
                final var currentZ = face.vertexIndices().stream()
                        .map(bffReader::getVertex)
                        .mapToDouble(Vertex::z)
                        .min()
                        .orElseThrow();

                if (currentZ < previousZ) {
                    throw new IllegalStateException("Faces are not sorted");
                }

                previousZ = currentZ;
            }
        }
    }
}
