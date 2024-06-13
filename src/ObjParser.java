import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjParser {
    private List<float[]> vertices;
    private List<int[]> faces;
    private List<float[]> textures;
    private List<int[]> textureFaces;

    public ObjParser() {
        vertices = new ArrayList<>();
        faces = new ArrayList<>();
        textures = new ArrayList<>();
        textureFaces = new ArrayList<>();
    }

    public void parse(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("v ")) {
                String[] parts = line.split(" ");
                float x = Float.parseFloat(parts[1]);
                float y = Float.parseFloat(parts[2]);
                float z = Float.parseFloat(parts[3]);
                vertices.add(new float[]{x, y, z});
            } else if (line.startsWith("vt ")) {
                String[] parts = line.split(" ");
                float u = Float.parseFloat(parts[1]);
                float v = Float.parseFloat(parts[2]);
                textures.add(new float[]{u, v});
            } else if (line.startsWith("f ")) {
                String[] parts = line.split(" ");
                int v1 = Integer.parseInt(parts[1].split("/")[0]);
                int v2 = Integer.parseInt(parts[2].split("/")[0]);
                int v3 = Integer.parseInt(parts[3].split("/")[0]);
                faces.add(new int[]{v1, v2, v3});

                int t1 = Integer.parseInt(parts[1].split("/")[1]);
                int t2 = Integer.parseInt(parts[2].split("/")[1]);
                int t3 = Integer.parseInt(parts[3].split("/")[1]);
                textureFaces.add(new int[]{t1, t2, t3});
            }
        }
        reader.close();
    }

    public List<float[]> getVertices() {
        return vertices;
    }

    public List<int[]> getFaces() {
        return faces;
    }

    public List<float[]> getTextures() {
        return textures;
    }

    public List<int[]> getTextureFaces() {
        return textureFaces;
    }
}
