import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphicsPanel extends JPanel {
    private JLabel objectName;
    private JLabel isShowingTextures;
    private JLabel isPaintedBlue;
    private JLabel currentScale;
    private JLabel currentPosition;
    private JLabel currentRotationInSite;
    private JLabel currentRotation;
    private Font textFont = new Font("Arial", Font.PLAIN, 10);

    private BufferedImage buffer;
    private float[][] zBuffer; // Z-buffer
    private BufferedImage texture;
    private ObjParser objParser;
    private Point3D center;
    private Point3D centerTemporal;
    private static final int PANEL_WIDTH = 700;
    private static final int PANEL_HEIGHT = 590;
    private double scaleFactor = 20.0; // Factor de escala inicial
    private boolean showTextures = false; // Mostrar texturas inicialmente apagado
    private boolean paintBlue = false; // Pintar objeto de color celeste
    private boolean showGrid = true;
    private boolean showAxes = true; // Mostrar ejes inicialmente
    private ArrayList<String> objectsList;
    private ArrayList<String> texturesList;
    private int index;
    private boolean hasTexture;

    public GraphicsPanel() {
        buffer = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        zBuffer = new float[PANEL_WIDTH][PANEL_HEIGHT]; // Inicializar Z-buffer
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setFocusable(true);
        setBackground(Color.black);

        objectsList = new ArrayList<>();
        objectsList.add("companionCube.obj");
        objectsList.add("sphere.obj");
        objectsList.add("cylinder.obj");
        objectsList.add("cone.obj");
        objectsList.add("miku01.obj");
        objectsList.add("Miku.obj");
        objectsList.add("len01.obj");

        texturesList = new ArrayList<>();
        texturesList.add("companionCube_tex.png");
        texturesList.add("");
        texturesList.add("");
        texturesList.add("");
        texturesList.add("miku01_tex.png");
        texturesList.add("Miku_tex.png");
        texturesList.add("len01_tex.png");

        index = 0;
        hasTexture = true;
        // Inicializamos los centros
        center = new Point3D(0, 0, 0, 100);
        centerTemporal = new Point3D(0, 0, 0, 100);

        objParser = new ObjParser();
        try {
            objParser.parse("companionCube.obj");
            texture = ImageIO.read(new File("companionCube_tex.png"));
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
                        center.setAngleX(center.getAngleX() + Math.toRadians(10));
                        break;
                    case KeyEvent.VK_G:
                        center.setAngleX(center.getAngleX() - Math.toRadians(10));
                        break;
                    case KeyEvent.VK_F:
                        center.setAngleY(center.getAngleY() + Math.toRadians(10));
                        break;
                    case KeyEvent.VK_H:
                        center.setAngleY(center.getAngleY() - Math.toRadians(10));
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
                        paintBlue = false; // Asegurar que no se pinte de celeste
                        break;
                    case KeyEvent.VK_2: // Mostrar texturas
                        if (hasTexture) {
                            showTextures = true;
                            paintBlue = false; // Asegurar que no se pinten de celeste
                        }
                        break;
                    case KeyEvent.VK_3: // Pintar objeto de color celeste
                        paintBlue = true;
                        showTextures = false; // Asegurar que no se muestren texturas
                        break;
                    case KeyEvent.VK_9:
                        index--;
                        showTextures = false;
                        paintBlue = false;
                        changeObject(index);
                        break;
                    case KeyEvent.VK_0:
                        index++;
                        showTextures = false;
                        paintBlue = false;
                        changeObject(index);
                        break;
                    case KeyEvent.VK_X: // Toggle axes visibility
                        showAxes = !showAxes;
                        showGrid = !showGrid;
                        break;
                    case KeyEvent.VK_5:
                        relocateObject();
                        break;
                }
                drawModel();
            }
        });
        initComponents();
    }

    private void initComponents() {
        setLayout(null);

        objectName = new JLabel("<html>Current object:<br>" + objectsList.get(index) + "</html>");
        objectName.setBounds(0, 10, 200, 40); // Ajusta la altura para acomodar el texto
        objectName.setFont(textFont);
        objectName.setForeground(Color.WHITE);

        isShowingTextures = new JLabel("<html>Showing textures:<br></html>");
        isShowingTextures.setBounds(0, 50, 200, 40); // Ajusta la altura
        isShowingTextures.setFont(textFont);
        isShowingTextures.setForeground(Color.WHITE);

        isPaintedBlue = new JLabel("<html>Painted blue:<br></html>");
        isPaintedBlue.setBounds(0, 90, 200, 40); // Ajusta la altura
        isPaintedBlue.setFont(textFont);
        isPaintedBlue.setForeground(Color.WHITE);

        currentScale = new JLabel("<html>Current scale:<br>" + scaleFactor + "</html>");
        currentScale.setBounds(0, 130, 200, 40); // Ajusta la altura
        currentScale.setFont(textFont);
        currentScale.setForeground(Color.WHITE);

        currentPosition = new JLabel("<html>Current position:<br>x: " + centerTemporal.getPointX() + "<br>y: " + centerTemporal.getPointY() + "<br>z: " + centerTemporal.getPointZ() + "</html>");
        currentPosition.setBounds(0, 170, 200, 60); // Ajusta la altura
        currentPosition.setFont(textFont);
        currentPosition.setForeground(Color.WHITE);

        currentRotation = new JLabel("<html>Current rotation:<br>x: " + centerTemporal.getAngleX() + "<br>y: " + centerTemporal.getAngleY() + "<br>z: " + centerTemporal.getAngleZ() + "</html>");
        currentRotation.setBounds(0, 230, 200, 60); // Ajusta la altura
        currentRotation.setFont(textFont);
        currentRotation.setForeground(Color.WHITE);

        currentRotationInSite = new JLabel("<html>Current rotation in site:<br>x: " + center.getAngleX() + "<br>y: " + center.getAngleY() + "<br>z: " + center.getAngleZ() + "</html>");
        currentRotationInSite.setBounds(0, 290, 200, 60); // Ajusta la altura
        currentRotationInSite.setFont(textFont);
        currentRotationInSite.setForeground(Color.WHITE);

        add(objectName);
        add(isShowingTextures);
        add(isPaintedBlue);
        add(currentScale);
        add(currentPosition);
        add(currentRotation);
        add(currentRotationInSite);


    }

    private void updateLabels() {
        objectName.setText("<html>Current object:<br>" + objectsList.get(index) + "</html>");
        if (showTextures) {
            isShowingTextures.setText("<html>Showing textures:<br> Yes </html>");
        } else {
            isShowingTextures.setText("<html>Showing textures:<br> No </html> ");
        }
        if (paintBlue) {
            isPaintedBlue.setText("<html>Painted blue:<br> Yes </html> ");
        } else {
            isPaintedBlue.setText("<html>Painted blue:<br> No </html> ");
        }

        currentScale.setText("<html>Current scale:<br>" + scaleFactor + "</html>");
        currentPosition.setText("<html>Current position:<br>x: " + centerTemporal.getPointX() + "<br>y: " + centerTemporal.getPointY() + "<br>z: " + centerTemporal.getPointZ() + "</html>");
        currentRotation.setText("<html>Current rotation:<br>x: " + centerTemporal.getAngleX() + "<br>y: " + centerTemporal.getAngleY() + "<br>z: " + centerTemporal.getAngleZ() + "</html>");
        currentRotationInSite.setText("<html>Current rotation in site:<br>x: " + center.getAngleX() + "<br>y: " + center.getAngleY() + "<br>z: " + center.getAngleZ() + "</html>");


    }

    private void changeObject(int index) {
        relocateObject();
        clearBuffer();
        objParser = new ObjParser();
        System.out.println(index);
        if (index < 0) {
            this.index = objectsList.size() - 1;
            System.out.println(index);
            index = this.index;
        }
        if (index > objectsList.size() - 1) {
            this.index = 0;
            System.out.println(index);
            index = this.index;
        }
        System.out.println(objectsList.get(index));
        System.out.println(texturesList.get(index));
        try {
            objParser.parse(objectsList.get(index));
            if (texturesList.get(index).isBlank()) {
                hasTexture = false;
            } else {
                hasTexture = true;
                texture = ImageIO.read(new File(texturesList.get(index)));
            }

            drawModel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void putPixel(int x, int y, float z, Color c) {
        if (x >= 0 && x < buffer.getWidth() && y >= 0 && y < buffer.getHeight()) {
            if (z < zBuffer[x][y]) {
                zBuffer[x][y] = z;
                buffer.setRGB(x, y, c.getRGB());
            }
        }
    }

    private Color getTextureColor(float u, float v) {
        int x = (int) (u * (texture.getWidth() - 1));
        int y = (int) ((1 - v) * (texture.getHeight() - 1));
        return new Color(texture.getRGB(x, y));
    }

    public void drawLineDDA(int x0, int y0, float z0, int x1, int y1, float z1, Color c) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        float xinc = (float) dx / steps;
        float yinc = (float) dy / steps;
        float zinc = (z1 - z0) / steps;

        float x = x0;
        float y = y0;
        float z = z0;

        putPixel(Math.round(x), Math.round(y), z, c);

        for (int k = 1; k < steps; k++) {
            x += xinc;
            y += yinc;
            z += zinc;
            putPixel(Math.round(x), Math.round(y), z, c);
        }
    }

    private void drawModel() {
        if (showAxes) {
            drawAxes(); // Dibujar ejes si showAxes es verdadero
        }

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

            if (paintBlue) {
                drawTriangle(p1Int, p2Int, p3Int, v1[2], v2[2], v3[2], Color.CYAN);
            } else if (showTextures) {
                drawTexturedTriangle(p1Int, p2Int, p3Int, v1[2], v2[2], v3[2], t1, t2, t3);
            } else {
                drawLineDDA(p1Int[0], p1Int[1], v1[2], p2Int[0], p2Int[1], v2[2], Color.WHITE);
                drawLineDDA(p2Int[0], p2Int[1], v2[2], p3Int[0], p3Int[1], v3[2], Color.WHITE);
                drawLineDDA(p3Int[0], p3Int[1], v3[2], p1Int[0], p1Int[1], v1[2], Color.WHITE);
            }
        }

        repaint();
    }

    private void drawTriangle(int[] p1, int[] p2, int[] p3, float z1, float z2, float z3, Color color) {
        int minX = Math.min(p1[0], Math.min(p2[0], p3[0]));
        int maxX = Math.max(p1[0], Math.max(p2[0], p3[0]));
        int minY = Math.min(p1[1], Math.min(p2[1], p3[1]));
        int maxY = Math.max(p1[1], Math.max(p2[1], p3[1]));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float[] barycentricCoords = getBarycentricCoordinates(p1, p2, p3, x, y);
                float alpha = barycentricCoords[0];
                float beta = barycentricCoords[1];
                float gamma = barycentricCoords[2];

                if (alpha >= 0 && beta >= 0 && gamma >= 0) {
                    float z = alpha * z1 + beta * z2 + gamma * z3;
                    putPixel(x, y, z, color);
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

    private void drawTexturedTriangle(int[] p1, int[] p2, int[] p3, float z1, float z2, float z3, float[] t1, float[] t2, float[] t3) {
        int minX = Math.min(p1[0], Math.min(p2[0], p3[0]));
        int maxX = Math.max(p1[0], Math.max(p2[0], p3[0]));
        int minY = Math.min(p1[1], Math.min(p2[1], p3[1]));
        int maxY = Math.max(p1[1], Math.max(p2[1], p3[1]));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float[] barycentricCoords = getBarycentricCoordinates(p1, p2, p3, x, y);
                float alpha = barycentricCoords[0];
                float beta = barycentricCoords[1];
                float gamma = barycentricCoords[2];

                if (alpha >= 0 && beta >= 0 && gamma >= 0) {
                    float z = alpha * z1 + beta * z2 + gamma * z3;
                    float u = alpha * t1[0] + beta * t2[0] + gamma * t3[0];
                    float v = alpha * t1[1] + beta * t2[1] + gamma * t3[1];
                    putPixel(x, y, z, getTextureColor(u, v));
                }
            }
        }
    }

    public void relocateObject() {
        // Reset the scale factor to its initial value
        scaleFactor = 50.0;

        // Reset the center and centerTemporal positions and angles to their initial values
        center = new Point3D(0, 0, 0, 100);
        centerTemporal = new Point3D(0, 0, 0, 100);

        // Reset the visibility settings for textures, axes, and color painting
        showTextures = false;
        paintBlue = false;
        showAxes = true;
        showGrid = true;

        // Clear the buffer and redraw the model
        clearBuffer();
        drawModel();
    }


    private void drawAxes() {
        // Centro de los ejes (mitad de la pantalla)
        Point3D origin = new Point3D(0, 0, 0, 1);
        Point2D origin2D = new Point2D.Double(PANEL_WIDTH / 2, PANEL_HEIGHT / 2);

        // Eje Y (verde): desde la parte superior central hasta el centro
        Point2D yAxis2D = new Point2D.Double(PANEL_WIDTH / 2, 0);
        drawLineDDA((int) origin2D.getX(), (int) origin2D.getY(), 0,
                (int) yAxis2D.getX(), (int) yAxis2D.getY(), 0, Color.GREEN);

        // Eje X (rojo): desde el lado derecho central hasta el centro
        Point2D xAxis2D = new Point2D.Double(PANEL_WIDTH, PANEL_HEIGHT / 2);
        drawLineDDA((int) origin2D.getX(), (int) origin2D.getY(), 0,
                (int) xAxis2D.getX(), (int) xAxis2D.getY(), 0, Color.RED);

        // Eje Z (azul): en diagonal entre el centro de la pantalla y la esquina superior derecha
        Point2D zAxis2D = new Point2D.Double(PANEL_WIDTH, 0);
        drawLineDDA((int) origin2D.getX(), (int) origin2D.getY(), 0,
                (int) zAxis2D.getX(), (int) zAxis2D.getY(), 0, Color.BLUE);
    }

    private void drawGrid() {
        int interval = 20; // Intervalo entre las líneas de la cuadrícula

        // Dibujar líneas verticales
        for (int x = 150; x <= PANEL_WIDTH; x += interval) {
            drawLineDDA(x, 0, 0, x, PANEL_HEIGHT, 0, Color.LIGHT_GRAY);
        }

        // Dibujar líneas horizontales
        for (int y = 0; y <= PANEL_HEIGHT; y += interval) {
            drawLineDDA(150, y, 0, PANEL_WIDTH, y, 0, Color.LIGHT_GRAY);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateLabels();
        if (showGrid) {
            drawGrid();
        }
        if (showAxes) {
            drawAxes(); 
        }
        g.drawImage(buffer, 0, 0, this);
    }

    public void clearBuffer() {
        Graphics2D g2d = buffer.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.dispose();

        // Inicializar Z-buffer con valores muy altos (infinito)
        for (int i = 0; i < PANEL_WIDTH; i++) {
            for (int j = 0; j < PANEL_HEIGHT; j++) {
                zBuffer[i][j] = Float.POSITIVE_INFINITY;
            }
        }
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

            panel.requestFocusInWindow(); // Asegúrate de que el panel tiene el foco del teclado

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
