import java.io.Serializable;

public class Sprite implements Serializable{
    private static final long serialVersionUID = 1L;
    private double x, y;

    public Sprite(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }


    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void move(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }
}
