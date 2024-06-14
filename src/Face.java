class Face implements Comparable<Face> {
    int[] vertices;
    int[] textureIndices;
    float depth;
    double[] normal;

    Face(int[] vertices, int[] textureIndices, float depth, double[] normal) {
        this.vertices = vertices;
        this.textureIndices = textureIndices;
        this.depth = depth;
        this.normal = normal;
    }

    @Override
    public int compareTo(Face other) {
        return Float.compare(other.depth, this.depth); // Orden descendente por profundidad
    }
}
