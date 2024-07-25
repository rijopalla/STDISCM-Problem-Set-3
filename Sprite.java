import java.io.Serializable;

public class Sprite implements Serializable{
    private double x, y;
    private double speed;

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

    public double getSpeed() {
        return speed;
    }

    public void move(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }
}
