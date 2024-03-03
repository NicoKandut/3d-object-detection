package nicok.bac.yolo3d.preprocessing;

import nicok.bac.yolo3d.off.Vertex;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public record LinearTransformation(
        double[][] transformation
) implements Transformation {

    public Vertex apply(final Vertex vertex) {
        final var vector = MatrixUtils.createRealMatrix(new double[][]{
                {vertex.x()},
                {vertex.y()},
                {vertex.z()},
                {1}
        });

        final var transformed = MatrixUtils.createRealMatrix(transformation)
                .multiply(vector)
                .getColumn(0);

        return new Vertex(
                transformed[0],
                transformed[1],
                transformed[2]
        );
    }

    public static class Builder {
        private RealMatrix scale = MatrixUtils.createRealMatrix(
                new double[][]{
                        {1, 0, 0},
                        {0, 1, 0},
                        {0, 0, 1}
                }
        );
        private Vertex shift = new Vertex(0, 0, 0);
        private RealMatrix rot = MatrixUtils.createRealMatrix(
                new double[][]{
                        {1, 0, 0},
                        {0, 1, 0},
                        {0, 0, 1}
                }
        );

        public Builder shift(final Vertex shift) {
            this.shift = shift;
            return this;
        }

        public Builder scaling(final double scale) {
            return scaling(new Vertex(scale, scale, scale));
        }

        public Builder scaling(final Vertex scale) {
            this.scale = MatrixUtils.createRealMatrix(new double[][]{
                            {scale.x(), 0, 0},
                            {0, scale.y(), 0},
                            {0, 0, scale.z()}
                    }
            );
            return this;
        }

        public Builder rotate(final double a, final double b, final double c) {
            final var rx = MatrixUtils.createRealMatrix(new double[][]{
                    {cos(a), -sin(a), 0},
                    {sin(a), cos(a), 0},
                    {0, 0, 1}
            });
            final var ry = MatrixUtils.createRealMatrix(new double[][]{
                    {cos(a), -sin(a), 0},
                    {sin(a), cos(a), 0},
                    {0, 0, 1}
            });
            final var rz = MatrixUtils.createRealMatrix(new double[][]{
                    {cos(a), -sin(a), 0},
                    {sin(a), cos(a), 0},
                    {0, 0, 1}
            });
            this.rot = rz.multiply(ry).multiply(rz);
            return this;
        }

        public LinearTransformation build() {
            final var scaleRot = scale.multiply(rot).getData();
            final var transform = new double[][]{
                    {scaleRot[0][0], scaleRot[0][1], scaleRot[0][2], shift.x()},
                    {scaleRot[1][0], scaleRot[1][1], scaleRot[1][2], shift.y()},
                    {scaleRot[2][0], scaleRot[2][1], scaleRot[2][2], shift.z()},
                    {0, 0, 0, 1}
            };

            return new LinearTransformation(transform);
        }
    }
}