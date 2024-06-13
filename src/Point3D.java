public class Point3D {
    private double x, y, z;
    private int size;
    private double angleX, angleY, angleZ;

    public Point3D(double x, double y, double z, int size) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.size = size;
    }

    public double getPointX() {
        return x;
    }

    public void setPointX(double x) {
        this.x = x;
    }

    public double getPointY() {
        return y;
    }

    public void setPointY(double y) {
        this.y = y;
    }

    public double getPointZ() {
        return z;
    }

    public void setPointZ(double z) {
        this.z = z;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getAngleX() {
        return angleX;
    }

    public void setAngleX(double angleX) {
        this.angleX = angleX;
    }

    public double getAngleY() {
        return angleY;
    }

    public void setAngleY(double angleY) {
        this.angleY = angleY;
    }

    public double getAngleZ() {
        return angleZ;
    }

    public void setAngleZ(double angleZ) {
        this.angleZ = angleZ;
    }
}

