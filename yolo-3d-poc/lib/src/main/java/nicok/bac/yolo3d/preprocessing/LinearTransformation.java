package nicok.bac.yolo3d.preprocessing;

import nicok.bac.yolo3d.off.Vertex;
import nicok.bac.yolo3d.off.Vertex;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public record LinearTransformation(RealMatrix transformation) implements Transformation {

    public Vertex apply(final Vertex vertex) {
        final var vector = MatrixUtils.createRealMatrix(new double[][]{
                {vertex.x()},
                {vertex.y()},
                {vertex.z()},
                {1}
        });

        final var transformed = transformation
                .multiply(vector)
                .getColumn(0);

        return new Vertex(
                transformed[0],
                transformed[1],
                transformed[2]
        );
    }

    public static class Builder {
        private Vertex center = new Vertex(0, 0, 0);
        private RealMatrix scale = MatrixUtils.createRealMatrix(
                new double[][]{
                        {1, 0, 0, 0},
                        {0, 1, 0, 0},
                        {0, 0, 1, 0},
                        {0, 0, 0, 1},
                }
        );
        private Vertex shift = new Vertex(0, 0, 0);
        private RealMatrix rot = MatrixUtils.createRealMatrix(
                new double[][]{
                        {1, 0, 0, 0},
                        {0, 1, 0, 0},
                        {0, 0, 1, 0},
                        {0, 0, 0, 1},
                }
        );

        public Builder shift(final Vertex shift) {
            this.shift = shift;
            return this;
        }

        public Builder rotationCenter(final Vertex center) {
            this.center = new Vertex(center.x(), center.y(), center.z());
            return this;
        }

        public Builder scaling(final double scale) {
            return scaling(new Vertex(scale, scale, scale));
        }

        public Builder scaling(final Vertex scale) {
            this.scale = MatrixUtils.createRealMatrix(new double[][]{
                            {scale.x(), 0, 0, 0},
                            {0, scale.y(), 0, 0},
                            {0, 0, scale.z(), 0},
                            {0, 0, 0, 1}
                    }
            );
            return this;
        }

        public Builder rotate(final double alpha, final double beta, final double gamma) {
            final var rz = MatrixUtils.createRealMatrix(new double[][]{
                    {cos(alpha), -sin(alpha), 0, 0},
                    {sin(alpha), cos(alpha), 0, 0},
                    {0, 0, 1, 0},
                    {0, 0, 0, 1},
            });
            final var ry = MatrixUtils.createRealMatrix(new double[][]{
                    {cos(beta), 0, sin(beta), 0},
                    {0, 1, 0, 0},
                    {-sin(beta), 0, cos(beta), 0},
                    {0, 0, 0, 1},
            });
            final var rx = MatrixUtils.createRealMatrix(new double[][]{
                    {1, 0, 0, 0},
                    {0, cos(gamma), -sin(gamma), 0},
                    {0, sin(gamma), cos(gamma), 0},
                    {0, 0, 0, 1},

            });


            this.rot = rz.multiply(ry).multiply(rx);
            return this;
        }

        public LinearTransformation build() {
            final var moveIntoOrigin = MatrixUtils.createRealMatrix(new double[][]{
                    {1, 0, 0, -center.x()},
                    {0, 1, 0, -center.y()},
                    {0, 0, 1, -center.z()},
                    {0, 0, 0, 1}
            });
            final var moveBack = MatrixUtils.createRealMatrix(new double[][]{
                    {1, 0, 0, center.x()},
                    {0, 1, 0, center.y()},
                    {0, 0, 1, center.z()},
                    {0, 0, 0, 1}
            });

            final var translate = MatrixUtils.createRealMatrix(new double[][]{
                    {1, 0, 0, shift.x()},
                    {0, 1, 0, shift.y()},
                    {0, 0, 1, shift.z()},
                    {0, 0, 0, 1}
            });

            // T * S * C * R * -C
            final var transform = translate
                    .multiply(scale)
                    .multiply(moveBack)
                    .multiply(rot)
                    .multiply(moveIntoOrigin);

            return new LinearTransformation(transform);
        }
    }
}