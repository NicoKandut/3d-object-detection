package nicok.bac.yolo3d.preprocessing;

import nicok.bac.yolo3d.off.Vertex;

public record PreProcessing(
        Vertex scaling
) {
    public Vertex scale(final Vertex vertex) {
        return Vertex.componentWiseMultiply(vertex, scaling);
    }

// TODO: better transformation with matrices

//    public Vertex switchYzAxis(final Vertex vertex) {
//        return new Vertex(vertex.x(), vertex.z(), vertex.y());
//    }

    public static class Builder {

        private Vertex factors = new Vertex(1, 1, 1);

        public Builder scaling(final Vertex factors) {
            this.factors = factors;
            return this;
        }

        public Builder scaling(final double factor) {
            this.factors = new Vertex(factor, factor, factor);
            return this;
        }

        public PreProcessing build() {
            return new PreProcessing(factors);
        }
    }
}