public class Sprite {
    private double x, y;
    private double speed;

    public Sprite(double x, double y, double speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
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
