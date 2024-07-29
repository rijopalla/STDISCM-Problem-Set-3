import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public class Canvas extends JPanel {
    private List<Particle> particles;
    private Sprite sprite;
    private int fps;
    private int totalFrames;
    private long lastFpsUpdateTime;

    public Canvas() {
        particles = new ArrayList<>();
        fps = 0;
    }

    public void addParticle(Particle p) {
        synchronized (particles) {
            particles.add(p);
        }
    }

    public List<Particle> getParticles() {
        synchronized (particles) {
            return new ArrayList<>(particles);
        }
    }

    public void updateSprite(Sprite sprite) {
        this.sprite = sprite;
        System.out.printf("Canvas updating sprite - X: %.2f, Y: %.2f%n", sprite.getX(), sprite.getY());
        repaint(); //trigger repaint for sprite update
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        long currentTime = System.currentTimeMillis();
        totalFrames++;

        //calculate FPS
        if (currentTime - lastFpsUpdateTime >= 500) {
            fps = (int) (totalFrames / ((currentTime - lastFpsUpdateTime) / 500.0));
            totalFrames = 0;
            lastFpsUpdateTime = currentTime;
        }

        //draw particles
        List<Particle> particlesToRemove = new ArrayList<>();
        synchronized (particles) {
            for (Particle particle : particles) {
                particle.update(this);
                if (particle.checkTarget()) {
                    particlesToRemove.add(particle);
                } else {
                    particle.draw(g, getHeight());
                }
            }
            particles.removeAll(particlesToRemove); //remove particles that reached their targets
        }

        //draw sprite if it exists
        if (sprite != null) {
            g.setColor(Color.BLUE);
            g.fillOval((int) sprite.getX() - 5, (int) sprite.getY() - 5, 10, 10);
        }

        //display FPS
        g.setColor(Color.BLACK);
        g.drawString("FPS: " + fps, 10, 10);
    }

    public void clearParticles() {
        synchronized (particles) {
            particles.clear();
        }
    }
}
