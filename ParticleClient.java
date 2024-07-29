import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ParticleClient extends JPanel {
    private List<Particle> particles;
    private Sprite sprite;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ParticleClient() {
        setPreferredSize(new Dimension(165, 95)); // Adjusted size
        sprite = new Sprite(640, 360); // Starting at center with default speed
        particles = new ArrayList<>();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
        setFocusable(true);
        requestFocusInWindow();

        connectToServer();
    }

    private void handleKeyPress(KeyEvent e) {
        System.out.println("Key pressed: " + e.getKeyCode());
        System.out.println("Before move: " + sprite.getX() + ", " + sprite.getY());
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                sprite.move(0, -10);
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                sprite.move(0, 10);
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                sprite.move(-10, 0);
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                sprite.move(10, 0);
                break;
        }

        System.out.println("After move: " + sprite.getX() + ", " + sprite.getY());
        sendSpritePosition();
        repaint();
    }

    private void connectToServer() {
        try {
            String serverIP = "server"; //replace with actual IP address if needed
            int serverPort = 12345;
            socket = new Socket(serverIP, serverPort); //connect to server
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            new Thread(new ServerListener()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendSpritePosition() {
        try {
            System.out.printf("Sending sprite position - X: %.2f, Y: %.2f%n", sprite.getX(), sprite.getY());
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
                    System.out.println("Received object: " + input.getClass().getName());
                    if (input instanceof List<?>) {
                        processParticleList((List<?>) input);
                    } else if (input instanceof Sprite) {
                        updateSprite((Sprite) input);
                    } else {
                        System.out.println("Unexpected object type received.");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                cleanUpResources();
            }
        }

        private void processParticleList(List<?> inputList) {
            List<Particle> newParticles = new ArrayList<>();
            for (Object obj : inputList) {
                System.out.println("Processing object: " + obj.getClass().getName());
                if (obj instanceof Particle) {
                    newParticles.add((Particle) obj);
                } else {
                    System.out.println("Non-particle object received: " + obj.getClass());
                }
            }
            particles = newParticles;
            System.out.println("Received particles: " + particles.size());
            SwingUtilities.invokeLater(() -> repaint());
        }

        private void updateSprite(Sprite newSprite) {
            sprite = newSprite;
            System.out.printf("Updated sprite position from server - X: %.2f, Y: %.2f%n", sprite.getX(), sprite.getY());
            SwingUtilities.invokeLater(() -> repaint());
        }

        private void cleanUpResources() {
            try {
                if (socket != null) socket.close();
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //draw sprite
        int spriteScreenX = getWidth() / 2;
        int spriteScreenY = getHeight() / 2;
        g.fillOval(spriteScreenX - 5, spriteScreenY - 5, 10, 10); // Draw sprite as a 10x10 oval

        //draw particles relative to the sprite's position
        if (particles != null) {
            for (Particle p : particles) {
                double dx = p.getX() - sprite.getX();
                double dy = p.getY() - sprite.getY();
                int screenX = (int) (spriteScreenX + dx);
                int screenY = (int) (spriteScreenY + dy);

                //check if particles are within the bounds of the panel
                if (screenX >= 0 && screenX < getWidth() && screenY >= 0 && screenY < getHeight()) {
                    g.fillOval(screenX - 2, screenY - 2, 5, 5); // Draw particles as 5x5 ovals
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Particle Simulation Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new ParticleClient());
            frame.pack();
            frame.setVisible(true);
        });
    }
}