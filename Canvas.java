import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

public class Canvas extends JPanel {
    private List<Particle> particles;
    private Sprite sprite;
    private int fps;
    private int frames;
    private long lastTime;
    private Timer fpsCounterTimer;
    private int totalFrames;
    private long lastFpsUpdateTime;

    public Canvas() {
        particles = new ArrayList<>();
        lastTime = System.currentTimeMillis();
        frames = 0;
        fps = 0;
        fpsCounterTimer = new Timer();
        fpsCounterTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFpsUpdateTime >= 500) {
                    fps = (int) (totalFrames / ((currentTime - lastFpsUpdateTime) / 500.0));
                    totalFrames = 0;
                    lastFpsUpdateTime = currentTime;
                    repaint(); // trigger repaint to update the FPS display
                }
            }
        }, 0, 500); // update FPS every 0.5 seconds
    }

    public void addParticle(Particle p) {
        synchronized (particles) {
            particles.add(p);
        }
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public void updateSprite(Sprite sprite) {
        this.sprite = sprite;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        totalFrames++;
        
        // Draw particles
        List<Particle> particlesToRemove = new ArrayList<>();
        synchronized (particles) {
            for (Particle particle : particles) {
                particle.update(this); // update the particle's position
                if (particle.checkTarget()) {
                    particlesToRemove.add(particle);
                } else if (particle.checkTargetTheta()) {
                    particlesToRemove.add(particle);
                } else if (particle.checkTargetVelocity()) {
                    particlesToRemove.add(particle);
                } else {
                    particle.draw(g, getHeight()); // draw the particle
                }
            }
            particles.removeAll(particlesToRemove); // remove particles that have reached the target
        }

        //draw sprite if it exists
        if (sprite != null) {
            g.setColor(Color.BLUE);
            g.fillOval((int) sprite.getX() - 5, (int) sprite.getY() - 5, 10, 10);
        }

        // Calculate FPS
        frames++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 500) { // update every 0.5 seconds
            fps = (int) (frames / ((currentTime - lastTime) / 1000.0)); // calculate FPS
            frames = 0;
            lastTime = currentTime;
        }

        // Display FPS
        g.setColor(Color.BLACK);
        g.drawString("FPS: " + fps, 10, 10);
    }

    // Method to clear particles
    public void clearParticles() {
        particles.clear();
    }
}