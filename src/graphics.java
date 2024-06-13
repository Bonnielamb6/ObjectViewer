import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class graphics extends JPanel {
    private BufferedImage buffer;
    private BufferedImage texture;
    private ObjParser objParser;
    private static final int PANEL_WIDTH = 600;
    private static final int PANEL_HEIGHT = 590;

    private float translateX = 0;
    private float translateY = 0;
    private float translateZ = 0;
    private float additionalScale = 1.0f;
    private float rotateX = 0;
    private float rotateY = 0;
    private float rotateZ = 0;

    private boolean showTexture = false;
    private boolean showWireframe = true;

    public graphics() {
        buffer = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setFocusable(true);

        objParser = new ObjParser();
        try {
            objParser.parse("miku01.obj");
            texture = ImageIO.read(new File("miku01_tex.png"));
            drawModel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:
                        translateX -= 0.1;
                        break;
                    case KeyEvent.VK_D:
                        translateX += 0.1;
                        break;
                    case KeyEvent.VK_W:
                        translateY += 0.1;
                        break;
                    case KeyEvent.VK_S:
                        translateY -= 0.1;
                        break;
                    case KeyEvent.VK_Q:
                        translateZ += 0.1;
                        break;
                    case KeyEvent.VK_E:
                        translateZ -= 0.1;
                        break;
                    case KeyEvent.VK_Z:
                        additionalScale *= 1.1;
                        break;
                    case KeyEvent.VK_C:
                        additionalScale /= 1.1;
                        break;
                    case KeyEvent.VK_J:
                        rotateX -= 5;
                        break;
                    case KeyEvent.VK_L:
                        rotateX += 5;
                        break;
                    case KeyEvent.VK_I:
                        rotateY -= 5;
                        break;
                    case KeyEvent.VK_K:
                        rotateY += 5;
                        break;
                    case KeyEvent.VK_U:
                        rotateZ -= 5;
                        break;
                    case KeyEvent.VK_O:
                        rotateZ += 5;
                        break;
                    case KeyEvent.VK_1:
                        showWireframe = true;
                        showTexture = false;
                        break;
                    case KeyEvent.VK_2:
                        showWireframe = false;
                        showTexture = true;
                        break;
                }
                clearBuffer();
                drawModel();
            }
        });
    }

    public void putPixel(int x, int y, Color c) {
        if (x >= 0 && x < buffer.getWidth() && y >= 0 && y < buffer.getHeight()) {
            buffer.setRGB(x, y, c.getRGB());
        }
    }

    private Color getTextureColor(float u, float v) {
        int x = (int) (u * (texture.getWidth() - 1));
        int y = (int) ((1 - v) * (texture.getHeight() - 1));
        return new Color(texture.getRGB(x, y));
    }

    public void drawLineDDA(int x0, int y0, int x1, int y1, Color c) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        float xinc = (float) dx / steps;
        float yinc = (float) dy / steps;

        float x = x0;
        float y = y0;

        putPixel(Math.round(x), Math.round(y), c);

        for (int k = 1; k <= steps; k++) {
            x += xinc;
            y += yinc;
            putPixel(Math.round(x), Math.round(y), c);
        }
    }

    private void drawModel() {
        List<float[]> vertices = objParser.getVertices();
        List<int[]> faces = objParser.getFaces();
        List<float[]> textures = objParser.getTextures();
        List<int[]> textureFaces = objParser.getTextureFaces();

        float[] center = {0, 0, 0};
        for (float[] vertex : vertices) {
            center[0] += vertex[0];
            center[1] += vertex[1];
            center[2] += vertex[2];
        }
        center[0] /= vertices.size();
        center[1] /= vertices.size();
        center[2] /= vertices.size();

        float maxDistance = 0;
        for (float[] vertex : vertices) {
            float distance = (float) Math.sqrt(Math.pow(vertex[0] - center[0], 2) + Math.pow(vertex[1] - center[1], 2) + Math.pow(vertex[2] - center[2], 2));
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }
        float scale = Math.min(PANEL_WIDTH, PANEL_HEIGHT) / (2 * maxDistance) * additionalScale;

        float cameraDistance = 100;

        for (int i = 0; i < faces.size(); i++) {
            int[] face = faces.get(i);
            int[] textureFace = textureFaces.get(i);

            float[] v1 = vertices.get(face[0] - 1);
            float[] v2 = vertices.get(face[1] - 1);
            float[] v3 = vertices.get(face[2] - 1);

            float[] t1 = textures.get(textureFace[0] - 1);
            float[] t2 = textures.get(textureFace[1] - 1);
            float[] t3 = textures.get(textureFace[2] - 1);

            int[] p1 = projectVertex(v1, center, scale, cameraDistance);
            int[] p2 = projectVertex(v2, center, scale, cameraDistance);
            int[] p3 = projectVertex(v3, center, scale, cameraDistance);

            if (showWireframe) {
                drawLineDDA(p1[0], p1[1], p2[0], p2[1], Color.BLACK);
                drawLineDDA(p2[0], p2[1], p3[0], p3[1], Color.BLACK);
                drawLineDDA(p3[0], p3[1], p1[0], p1[1], Color.BLACK);
            } else if (showTexture) {
                drawTexturedTriangle(p1, p2, p3, t1, t2, t3);
            }
        }

        repaint();
    }

    private int[] projectVertex(float[] vertex, float[] center, float scale, float cameraDistance) {
        float x = vertex[0] - center[0];
        float y = vertex[1] - center[1];
        float z = vertex[2] - center[2];

        // Apply translation
        x += translateX;
        y += translateY;
        z += translateZ;

        // Apply rotations
        float radX = (float) Math.toRadians(rotateX);
        float radY = (float) Math.toRadians(rotateY);
        float radZ = (float) Math.toRadians(rotateZ);

        // Rotation around X axis
        float cosX = (float) Math.cos(radX);
        float sinX = (float) Math.sin(radX);
        float tempY = y * cosX - z * sinX;
        float tempZ = y * sinX + z * cosX;
        y = tempY;
        z = tempZ;

        // Rotation around Y axis
        float cosY = (float) Math.cos(radY);
        float sinY = (float) Math.sin(radY);
        float tempX = x * cosY + z * sinY;
        tempZ = -x * sinY + z * cosY;
        x = tempX;
        z = tempZ;

        // Rotation around Z axis
        float cosZ = (float) Math.cos(radZ);
        float sinZ = (float) Math.sin(radZ);
        tempX = x * cosZ - y * sinZ;
        tempY = x * sinZ + y * cosZ;
        x = tempX;
        y = tempY;

        // Apply perspective projection
        float perspectiveScale = cameraDistance / (cameraDistance - z);
        x *= perspectiveScale;
        y *= perspectiveScale;

        int screenX = (int) (x * scale + PANEL_WIDTH / 2);
        int screenY = (int) (-y * scale + PANEL_HEIGHT / 2);

        return new int[]{screenX, screenY};
    }

    private void drawTexturedTriangle(int[] p1, int[] p2, int[] p3, float[] t1, float[] t2, float[] t3) {
        // Bounding box of the triangle
        int minX = Math.min(p1[0], Math.min(p2[0], p3[0]));
        int maxX = Math.max(p1[0], Math.max(p2[0], p3[0]));
        int minY = Math.min(p1[1], Math.min(p2[1], p3[1]));
        int maxY = Math.max(p1[1], Math.max(p2[1], p3[1]));

        // Iterate over the bounding box
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float[] barycentricCoords = getBarycentricCoordinates(p1, p2, p3, x, y);
                float alpha = barycentricCoords[0];
                float beta = barycentricCoords[1];
                float gamma = barycentricCoords[2];

                if (alpha >= 0 && beta >= 0 && gamma >= 0) {
                    float u = alpha * t1[0] + beta * t2[0] + gamma * t3[0];
                    float v = alpha * t1[1] + beta * t2[1] + gamma * t3[1];
                    putPixel(x, y, getTextureColor(u, v));
                }
            }
        }
    }

    private float[] getBarycentricCoordinates(int[] p1, int[] p2, int[] p3, int px, int py) {
        float det = (p2[1] - p3[1]) * (p1[0] - p3[0]) + (p3[0] - p2[0]) * (p1[1] - p3[1]);
        float alpha = ((p2[1] - p3[1]) * (px - p3[0]) + (p3[0] - p2[0]) * (py - p3[1])) / det;
        float beta = ((p3[1] - p1[1]) * (px - p3[0]) + (p1[0] - p3[0]) * (py - p3[1])) / det;
        float gamma = 1 - alpha - beta;
        return new float[]{alpha, beta, gamma};
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(buffer, 0, 0, this);
    }

    public void clearBuffer() {
        Graphics2D g2d = buffer.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame win = new JFrame();
            win.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            win.setResizable(false);
            win.setSize(600, 600);
            win.setLayout(new BorderLayout());
            win.setLocationRelativeTo(null);

            graphics panel = new graphics();
            win.add(panel, BorderLayout.CENTER);

            win.pack();
            win.setVisible(true);

            panel.requestFocusInWindow();
        });
    }
}