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
    private Sprite localSprite;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private List<Sprite> sprites = new ArrayList<>();
    private int fps;
    private int totalFrames;
    private long lastFpsUpdateTime;

    public ParticleClient() {
        setPreferredSize(new Dimension(165, 95)); // Adjusted size
        setDoubleBuffered(true);
        Point initPos = getUserInitialPosition();
        localSprite = new Sprite(initPos.x, initPos.y);
        sprites.add(localSprite);
        particles = new ArrayList<>();
        fps = 0;
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
        System.out.println("Before move: " + localSprite.getX() + ", " + localSprite.getY());
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                localSprite.move(0, -10);
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                localSprite.move(0, 10);
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                localSprite.move(-10, 0);
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                localSprite.move(10, 0);
                break;
        }

        System.out.println("After move: " + localSprite.getX() + ", " + localSprite.getY());
        sendSpritePosition();
        repaint();
    }

    private void connectToServer() {
        boolean running = true;
        new Thread(() -> {
            while (running) {
                try {
                    String serverIP = "localhost"; //replace with actual IP address if needed
                    int serverPort = 12345;
                    socket = new Socket(serverIP, serverPort); //connect to server
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());

                    new Thread(new ServerListener()).start();
                    break; // exit the loop if connected successfully
                } catch (IOException e) {
                    e.printStackTrace();
                    cleanUpResources();
                    try {
                        Thread.sleep(5000); // wait before trying to reconnect
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void sendSpritePosition() {
        try {
            System.out.printf("Sending sprite position - X: %.2f, Y: %.2f%n", 
                                localSprite.getX(), localSprite.getY());
            out.writeUnshared(localSprite);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            cleanUpResources(); // Ensure resources are cleaned up properly on error
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
                        List<?> inputList = (List<?>) input;
                        if (!inputList.isEmpty() && inputList.get(0) instanceof Particle) {
                            processParticleList(inputList);
                        } else if (!inputList.isEmpty() && inputList.get(0) instanceof Sprite) {
                            processSpriteList(inputList);
                        }
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

        private void processSpriteList(List<?> inputList) {
            List<Sprite> newSprites = new ArrayList<>();
            for (Object obj : inputList) {
                if (obj instanceof Sprite) {
                    Sprite receivedSprite = (Sprite) obj;
                    if (!receivedSprite.equals(localSprite)) {
                        newSprites.add(receivedSprite);
                    }
                }
            }
            sprites = newSprites;
            sprites.add(localSprite); //ensure the local sprite is included
            SwingUtilities.invokeLater(() -> repaint());
        }

        private void processParticleList(List<?> inputList) {
            List<Particle> newParticles = new ArrayList<>();
            for (Object obj : inputList) {
                if (obj instanceof Particle) {
                    Particle particle = (Particle) obj;
                    System.out.printf("Processing Particle - X: %.2f, Y: %.2f%n", particle.getX(), particle.getY());
                    newParticles.add(particle);
                } else {
                    System.out.println("Unexpected object in particle list: " + obj.getClass().getName());
                }
            }
            particles = newParticles;
            System.out.println("Received particles: " + particles.size());

            if (!particles.isEmpty()) {
                Particle firstParticle = particles.get(0);
                System.out.printf("First particle X: %.2f, Y: %.2f%n", firstParticle.getX(), firstParticle.getY());
            }
            System.out.println("Requesting repaint due to new particles.");
            SwingUtilities.invokeLater(() -> repaint());
        }

    }

    private void cleanUpResources() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        long currentTime = System.currentTimeMillis();
        totalFrames++;

        //draw sprite
        int spriteScreenX = getWidth() / 2;
        int spriteScreenY = getHeight() / 2;

        int centerX = getWidth() / 2; // Center of the JPanel
        int centerY = getHeight() / 2;
        g.fillOval(spriteScreenX - 5, spriteScreenY - 5, 10, 10); // Draw sprite as a 10x10 oval

        double scaleFactor = Math.min(getWidth(), getHeight()) / 100.0; //
        //draw other sprites
        for (Sprite s : sprites) {
            if (s == localSprite) continue;

            double dx = s.getX() - localSprite.getX();
            double dy = s.getY() - localSprite.getY();
            int screenX = (int) (spriteScreenX - dx);
            int screenY = (int) (spriteScreenY - dy);
            
            g.fillOval(screenX - 5, screenY - 5, 10, 10);
        }

        //draw particles relative to the sprite's position
        if (particles != null) {
            for (Particle p : particles) {
                double dx = (p.getX() - localSprite.getX()) * scaleFactor;
                double dy = (p.getY() - localSprite.getY()) * scaleFactor;
                int screenX = (int) (centerX - dx);
                int screenY = (int) (centerY - dy);
            
                System.out.printf("Drawing particle at X: %d, Y: %d%n", screenX, screenY); // Debugging line
            
                if (screenX >= 0 && screenX < getWidth() && screenY >= 0 && screenY < getHeight()) {
                    g.fillOval(screenX - 2, screenY - 2, 5, 5);
                }
            }
        }

        //calculate FPS
        if (currentTime - lastFpsUpdateTime >= 500) {
            fps = (int) (totalFrames / ((currentTime - lastFpsUpdateTime) / 500.0));
            totalFrames = 0;
            lastFpsUpdateTime = currentTime;
        }

        //display FPS
        g.setColor(Color.BLACK);
        g.drawString("FPS: " + fps, 10, 10);
    }

    private Point getUserInitialPosition() {
        int x = 640; // default value
        int y = 360; // default value

        try {
            String xPos = JOptionPane.showInputDialog(this, "Enter initial X position:", "640");
            String yPos = JOptionPane.showInputDialog(this, "Enter initial Y position:", "360");

            if (xPos != null && !xPos.isEmpty()) {
                x = Integer.parseInt(xPos);
            }
            if (yPos != null && !yPos.isEmpty()) {
                y = Integer.parseInt(yPos);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Using default position.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return new Point(x, y);
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