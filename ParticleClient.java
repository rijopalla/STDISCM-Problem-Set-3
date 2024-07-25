import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ParticleClient extends JPanel {
    private List<Particle> particles;
    private Sprite sprite;
    private Timer particleUpdateTimer;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ParticleClient() {
        setPreferredSize(new Dimension(33 * 5, 19 * 5)); // 165x95 pixels
        sprite = new Sprite(640, 360, 5); // Starting at center with speed 5

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> sprite.move(0, sprite.getSpeed());
                    case KeyEvent.VK_S -> sprite.move(0, -sprite.getSpeed());
                    case KeyEvent.VK_A -> sprite.move(-sprite.getSpeed(), 0);
                    case KeyEvent.VK_D -> sprite.move(sprite.getSpeed(), 0);
                }
                sendSpritePosition();
                repaint();
            }
        });
        setFocusable(true);
        connectToServer();
        startParticleUpdates();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345); // Server IP and port
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            new Thread(new ServerListener()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startParticleUpdates() {
        if (particleUpdateTimer == null) {
            particleUpdateTimer = new Timer();
            particleUpdateTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateParticles();
                    repaint();
                }
            }, 0, 16); // approximately 60 FPS
        }
    }

    private void updateParticles() {
        // Update particles received from server
    }

    private void sendSpritePosition() {
        try {
            out.writeObject(sprite);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Object input = in.readObject();
                    if (input instanceof List) {
                        // Update particle list
                        particles = (List<Particle>) input;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background, sprite, and particles here
        // Draw the sprite in the center of the screen
        int spriteScreenX = getWidth() / 2;
        int spriteScreenY = getHeight() / 2;
        g.fillOval(spriteScreenX - 5, spriteScreenY - 5, 10, 10); // Draw the sprite as a 10x10 oval
        // Draw particles relative to the sprite's position
        if (particles != null) {
            for (Particle p : particles) {
                double dx = p.getX() - sprite.getX();
                double dy = p.getY() - sprite.getY();
                int screenX = (int) (spriteScreenX + dx);
                int screenY = (int) (spriteScreenY + dy);
                if (screenX >= 0 && screenX < getWidth() && screenY >= 0 && screenY < getHeight()) {
                    g.fillOval(screenX - 2, screenY - 2, 5, 5); // Draw particles as 5x5 ovals
                }
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Particle Simulation Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ParticleClient());
        frame.pack();
        frame.setVisible(true);
    }
}
