import java.awt.*;
import java.io.Serializable;

public class Particle implements Serializable {
    private static final long serialVersionUID = 1L; // Unique ID for serialization

    private double x, y, velocity, theta;
    private double targetTheta;
    private double targetX, targetY;
    private double targetVelocity;
    private int diameter = 5; // particle size
    private boolean hasTarget; // if added through batch options
    private boolean isBatch3 = false; // added through the third batch option

    public Particle(double x, double y, double velocity, double theta) {
        this(x, y, velocity, theta, Double.NaN); // call overloaded constructor with NaN for endTheta
    }

    public Particle(double x, double y, double velocity, double theta, double targetTheta) {
        this.x = x;
        this.y = y;
        this.velocity = velocity;
        this.theta = Math.toRadians(theta); // convert to radians
        this.targetTheta = Math.toRadians(targetTheta); // convert to radians
        this.hasTarget = false;
    }

    public Particle(double x, double y, double velocity, double theta, double targetX, double targetY) {
        this.x = x;
        this.y = y;
        this.velocity = velocity;
        this.targetX = targetX;
        this.targetY = targetY;
        this.hasTarget = true;
        computeTheta();
    }

    public void update(Canvas canvas) {
        x += velocity * Math.cos(theta);
        y += velocity * Math.sin(theta);

        // Check if the particle has reached or exceeded targetTheta
        if (!Double.isNaN(targetTheta) && Math.abs(theta - targetTheta) < 0.01) {
            hasTarget = true;
            targetX = x;
            targetY = y;
        }

        // Check for boundary collision and reflect
        if (x <= 0 || x >= canvas.getWidth() - diameter) {
            theta = Math.PI - theta; // reflect horizontally
        }
        if (y <= 0 || y >= canvas.getHeight() - diameter) {
            theta = -theta; // reflect vertically
        }

        // Make sure particles stay within boundaries after reflection
        x = Math.max(0, Math.min(x, canvas.getWidth() - diameter));
        y = Math.max(0, Math.min(y, canvas.getHeight() - diameter));
    }

    public void draw(Graphics g, int canvasHeight) { 
        int drawY = canvasHeight - (int)y - diameter; // Invert y-coordinates to meet specs where coordinate (0,0) should be on the bottom left
        g.fillOval((int)x, drawY, diameter, diameter); 
    }

    public boolean checkTarget() {
        if (!hasTarget) return false;
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < velocity) {
            x = targetX;
            y = targetY;
            return true; // target has been reached
        }

        if (Math.abs(theta - targetTheta) < 0.01) {
            x = targetX;
            y = targetY;
            return true;
        }

        x += velocity * dx / distance;
        y += velocity * dy / distance;
        return false; // target not yet reached
    }

    public boolean checkTargetVelocity() {
        return Math.abs(velocity - targetVelocity) < 0.01;
    }

    public boolean checkTargetTheta() {
        if (!hasTarget) return false;
        return Math.abs(theta - targetTheta) < 0.01; 
    }

    private void computeTheta() {
        double dx = targetX - x;
        double dy = targetY - y;
        // Add a small epsilon value to dx to avoid division by zero
        double epsilon = 1e-6;
        if (Math.abs(dx) < epsilon && Math.abs(dy) < epsilon) {
            // If the target is at the starting position, set theta to 0
            this.theta = 0;
        } else {
            this.theta = Math.atan2(dy, dx);
        }
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public int getDiameter() {
        return diameter;
    }

    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }

    public boolean isBatch3() {
        return isBatch3;
    }

    public double getVelocity() {
        return this.velocity;
    }

    public double getTheta() {
        return this.theta;
    }

    public void setIsBatch3(boolean isBatch3) {
        this.isBatch3 = isBatch3;
    }

    public void setTargetVelocity(double targetVelocity) {
        this.targetVelocity = targetVelocity;
        this.hasTarget = true;
    }
}
