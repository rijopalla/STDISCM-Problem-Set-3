import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public class Canvas extends JPanel {
    private List<Particle> particles;
    private List<Sprite> sprites;
    private int fps;
    private int totalFrames;
    private long lastFpsUpdateTime;

    public Canvas() {
        particles = new ArrayList<>();
        fps = 0;
        this.sprites = new ArrayList<>();
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

    public void updateSprites(List<Sprite> sprites) {
        this.sprites = sprites;
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

        //draw sprites 
        for (Sprite sprite: sprites) {
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
