import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class GraphicsPanel extends JPanel {
    private BufferedImage buffer;
    private BufferedImage texture;
    private ObjParser objParser;
    private Point3D center;
    private Point3D centerTemporal;
    private static final int PANEL_WIDTH = 600;
    private static final int PANEL_HEIGHT = 590;
    private double scaleFactor = 5.0; // Factor de escala inicial
    private boolean showTextures = false; // Mostrar texturas inicialmente apagado

    public GraphicsPanel() {
        buffer = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setFocusable(true);

        // Inicializamos los centros
        center = new Point3D(0, 0, 0, 100);
        centerTemporal = new Point3D(0, 0, 0, 100);

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
                clearBuffer();
                switch (e.getKeyCode()) {
                    // MOVE
                    case KeyEvent.VK_W: // up
                        centerTemporal.setPointY(centerTemporal.getPointY() + 10);
                        break;
                    case KeyEvent.VK_A: // left
                        centerTemporal.setPointX(centerTemporal.getPointX() - 10);
                        break;
                    case KeyEvent.VK_S: // down
                        centerTemporal.setPointY(centerTemporal.getPointY() - 10);
                        break;
                    case KeyEvent.VK_D: // right
                        centerTemporal.setPointX(centerTemporal.getPointX() + 10);
                        break;
                    case KeyEvent.VK_Q: // forward
                        centerTemporal.setPointZ(centerTemporal.getPointZ() + 10);
                        break;
                    case KeyEvent.VK_E: // backward
                        centerTemporal.setPointZ(centerTemporal.getPointZ() - 10);
                        break;
                    // RESIZE
                    case KeyEvent.VK_Z:
                        scaleFactor *= 1.1;
                        break;
                    case KeyEvent.VK_C:
                        scaleFactor /= 1.1;
                        break;
                    // ROTATE GLOBAL
                    case KeyEvent.VK_I:
                        centerTemporal.setAngleX(centerTemporal.getAngleX() + Math.toRadians(10));
                        break;
                    case KeyEvent.VK_K:
                        centerTemporal.setAngleX(centerTemporal.getAngleX() - Math.toRadians(10));
                        break;
                    case KeyEvent.VK_J:
                        centerTemporal.setAngleY(centerTemporal.getAngleY() + Math.toRadians(10));
                        break;
                    case KeyEvent.VK_L:
                        centerTemporal.setAngleY(centerTemporal.getAngleY() - Math.toRadians(10));
                        break;
                    case KeyEvent.VK_U:
                        centerTemporal.setAngleZ(centerTemporal.getAngleZ() + Math.toRadians(10));
                        break;
                    case KeyEvent.VK_O:
                        centerTemporal.setAngleZ(centerTemporal.getAngleZ() - Math.toRadians(10));
                        break;
                    // ROTATE LOCAL
                    case KeyEvent.VK_T:
                        center.setAngleY(center.getAngleY() + Math.toRadians(10));
                        break;
                    case KeyEvent.VK_G:
                        center.setAngleY(center.getAngleY() - Math.toRadians(10));
                        break;
                    case KeyEvent.VK_F:
                        center.setAngleX(center.getAngleX() + Math.toRadians(10));
                        break;
                    case KeyEvent.VK_H:
                        center.setAngleX(center.getAngleX() - Math.toRadians(10));
                        break;
                    case KeyEvent.VK_R:
                        center.setAngleZ(center.getAngleZ() + Math.toRadians(10));
                        break;
                    case KeyEvent.VK_Y:
                        center.setAngleZ(center.getAngleZ() - Math.toRadians(10));
                        break;
                    // TEXTURES
                    case KeyEvent.VK_1: // Mostrar solo enmallado
                        showTextures = false;
                        break;
                    case KeyEvent.VK_2: // Mostrar texturas
                        showTextures = true;
                        break;
                }
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
        u = Math.min(Math.max(u, 0), 1); // Clamping u to the range [0, 1]
        v = Math.min(Math.max(v, 0), 1); // Clamping v to the range [0, 1]

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

        for (int k = 1; k < steps; k++) {
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

        for (int i = 0; i < faces.size(); i++) {
            int[] face = faces.get(i);
            int[] textureFace = textureFaces.get(i);

            float[] v1 = vertices.get(face[0] - 1);
            float[] v2 = vertices.get(face[1] - 1);
            float[] v3 = vertices.get(face[2] - 1);

            float[] t1 = textures.get(textureFace[0] - 1);
            float[] t2 = textures.get(textureFace[1] - 1);
            float[] t3 = textures.get(textureFace[2] - 1);

            Point2D p1 = transformAndProject(v1);
            Point2D p2 = transformAndProject(v2);
            Point2D p3 = transformAndProject(v3);

            int[] p1Int = {(int) p1.getX(), (int) p1.getY()};
            int[] p2Int = {(int) p2.getX(), (int) p2.getY()};
            int[] p3Int = {(int) p3.getX(), (int) p3.getY()};

            if (showTextures) {
                drawTexturedTriangle(p1Int, p2Int, p3Int, t1, t2, t3);
            } else {
                drawLineDDA(p1Int[0], p1Int[1], p2Int[0], p2Int[1], Color.BLACK);
                drawLineDDA(p2Int[0], p2Int[1], p3Int[0], p3Int[1], Color.BLACK);
                drawLineDDA(p3Int[0], p3Int[1], p1Int[0], p1Int[1], Color.BLACK);
            }
        }

        repaint();
    }

    private Point2D transformAndProject(float[] vertex) {
        double x = vertex[0] * scaleFactor;
        double y = vertex[1] * scaleFactor;
        double z = vertex[2] * scaleFactor;

        // Apply rotation around the object's own axes
        double[] rotatedLocalX = rotateX(new double[]{x, y, z}, center.getAngleX());
        double[] rotatedLocalXY = rotateY(rotatedLocalX, center.getAngleY());
        double[] rotatedLocalXYZ = rotateZ(rotatedLocalXY, center.getAngleZ());

        x = rotatedLocalXYZ[0];
        y = rotatedLocalXYZ[1];
        z = rotatedLocalXYZ[2];

        // Apply translation
        x += centerTemporal.getPointX();
        y += centerTemporal.getPointY();
        z += centerTemporal.getPointZ();

        // Apply rotations
        double[] rotatedGlobalX = rotateX(new double[]{x, y, z}, centerTemporal.getAngleX());
        double[] rotatedGlobalXY = rotateY(rotatedGlobalX, centerTemporal.getAngleY());
        double[] rotatedGlobalXYZ = rotateZ(rotatedGlobalXY, centerTemporal.getAngleZ());

        return point3Dto2D(rotatedGlobalXYZ[0], rotatedGlobalXYZ[1], rotatedGlobalXYZ[2]);
    }

    private Point2D point3Dto2D(double x, double y, double z) {
        int zProjection = 100; // Distance for the projection
        double u = -(z / (double) zProjection); // Calculamos el factor de U
        double px = x + 1 * u;
        double py = y + 1 * u;

        return new Point2D.Double(px + PANEL_WIDTH / 2, -py + PANEL_HEIGHT / 2);
    }

    public double[] rotateX(double[] vertex, double angle) {
        double[] rotated = new double[3];
        rotated[0] = vertex[0];
        rotated[1] = vertex[1] * Math.cos(angle) - vertex[2] * Math.sin(angle);
        rotated[2] = vertex[1] * Math.sin(angle) + vertex[2] * Math.cos(angle);
        return rotated;
    }

    public double[] rotateY(double[] vertex, double angle) {
        double[] rotated = new double[3];
        rotated[0] = vertex[0] * Math.cos(angle) + vertex[2] * Math.sin(angle);
        rotated[1] = vertex[1];
        rotated[2] = -vertex[0] * Math.sin(angle) + vertex[2] * Math.cos(angle);
        return rotated;
    }

    public double[] rotateZ(double[] vertex, double angle) {
        double[] rotated = new double[3];
        rotated[0] = vertex[0] * Math.cos(angle) - vertex[1] * Math.sin(angle);
        rotated[1] = vertex[0] * Math.sin(angle) + vertex[1] * Math.cos(angle);
        rotated[2] = vertex[2];
        return rotated;
    }

    private void drawTexturedTriangle(int[] p1, int[] p2, int[] p3, float[] t1, float[] t2, float[] t3) {
        // Ordenar los puntos por coordenada Y (p1, p2, p3)
        if (p1[1] > p2[1]) {
            int[] tmp = p1; p1 = p2; p2 = tmp;
            float[] tmpT = t1; t1 = t2; t2 = tmpT;
        }
        if (p2[1] > p3[1]) {
            int[] tmp = p2; p2 = p3; p3 = tmp;
            float[] tmpT = t2; t2 = t3; t3 = tmpT;
        }
        if (p1[1] > p2[1]) {
            int[] tmp = p1; p1 = p2; p2 = tmp;
            float[] tmpT = t1; t1 = t2; t2 = tmpT;
        }

        // Calcular las pendientes
        float invslope1 = (p2[0] - p1[0]) / (float)(p2[1] - p1[1]);
        float invslope2 = (p3[0] - p1[0]) / (float)(p3[1] - p1[1]);

        // Calcular las pendientes de las coordenadas de textura
        float invslopeU1 = (t2[0] - t1[0]) / (p2[1] - p1[1]);
        float invslopeU2 = (t3[0] - t1[0]) / (p3[1] - p1[1]);
        float invslopeV1 = (t2[1] - t1[1]) / (p2[1] - p1[1]);
        float invslopeV2 = (t3[1] - t1[1]) / (p3[1] - p1[1]);

        float curx1 = p1[0];
        float curx2 = p1[0];

        float curu1 = t1[0];
        float curu2 = t1[0];
        float curv1 = t1[1];
        float curv2 = t1[1];

        // Desde y1 hasta y2
        for (int scanlineY = p1[1]; scanlineY <= p2[1]; scanlineY++) {
            drawScanline(scanlineY, (int)curx1, (int)curx2, curu1, curu2, curv1, curv2);
            curx1 += invslope1;
            curx2 += invslope2;
            curu1 += invslopeU1;
            curu2 += invslopeU2;
            curv1 += invslopeV1;
            curv2 += invslopeV2;
        }

        // Calcular la nueva pendiente
        invslope1 = (p3[0] - p2[0]) / (float)(p3[1] - p2[1]);
        invslopeU1 = (t3[0] - t2[0]) / (p3[1] - p2[1]);
        invslopeV1 = (t3[1] - t2[1]) / (p3[1] - p2[1]);

        curx1 = p2[0];
        curu1 = t2[0];
        curv1 = t2[1];

        // Desde y2 hasta y3
        for (int scanlineY = p2[1]; scanlineY <= p3[1]; scanlineY++) {
            drawScanline(scanlineY, (int)curx1, (int)curx2, curu1, curu2, curv1, curv2);
            curx1 += invslope1;
            curx2 += invslope2;
            curu1 += invslopeU1;
            curu2 += invslopeU2;
            curv1 += invslopeV1;
            curv2 += invslopeV2;
        }
    }

    private void drawScanline(int y, int x1, int x2, float u1, float u2, float v1, float v2) {
        if (x1 > x2) {
            int tmpX = x1; x1 = x2; x2 = tmpX;
            float tmpU = u1; u1 = u2; u2 = tmpU;
            float tmpV = v1; v1 = v2; v2 = tmpV;
        }

        float invslopeU = (u2 - u1) / (x2 - x1);
        float invslopeV = (v2 - v1) / (x2 - x1);

        float curU = u1;
        float curV = v1;

        for (int x = x1; x <= x2; x++) {
            if (x >= 0 && x < buffer.getWidth() && y >= 0 && y < buffer.getHeight()) {
                putPixel(x, y, getTextureColor(curU, curV));
            }
            curU += invslopeU;
            curV += invslopeV;
        }
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

            GraphicsPanel panel = new GraphicsPanel();
            win.add(panel, BorderLayout.CENTER);

            win.pack();
            win.setVisible(true);

            panel.requestFocusInWindow();  // Asegúrate de que el panel tiene el foco del teclado

            Point3D point = new Point3D(0, 0, 0, 100);
            panel.setCenter(point);
            Point3D pointTemporal = new Point3D(0, 0, 0, 100);
            panel.setCenterTemporal(pointTemporal);
            panel.drawModel();
        });
    }

    public Point3D getCenter() {
        return center;
    }

    public void setCenter(Point3D center) {
        this.center = center;
    }

    public Point3D getCenterTemporal() {
        return centerTemporal;
    }

    public void setCenterTemporal(Point3D centerTemporal) {
        this.centerTemporal = centerTemporal;
    }
}
