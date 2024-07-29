import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
// import java.util.Timer;
// import java.util.TimerTask;

public class ParticleClient extends JPanel {
    private List<Particle> particles;
    private Sprite sprite;
    // private Timer particleUpdateTimer;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ParticleClient() {
        setPreferredSize(new Dimension(33 * 5, 19 * 5)); // 165x95 pixels
        sprite = new Sprite(640, 360); // Starting at center with speed 5
        particles = new ArrayList<>();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
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
        });
        setFocusable(true);
        requestFocusInWindow();

        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345); //server IP and port
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
                        System.out.println("Received list");
                        List<?> inputList = (List<?>) input;
                        System.out.println("List size: " + inputList.size());
                        List<Particle> newParticles = new ArrayList<>();
                        for (Object obj : inputList) {
                            System.out.println("Processing object: " + obj.getClass().getName());
                            if (obj instanceof Particle) {
                                System.out.println("Received particle: " + obj);
                                newParticles.add((Particle) obj);
                            } else {
                                System.out.println("Non-particle object received: " + obj.getClass());
                            }
                        }
                        particles = newParticles;
                        System.out.println("Received particles: " + particles.size());
                        repaint();
                    } 
                    else if (input instanceof Sprite) {
                        sprite = (Sprite) input;
                        System.out.printf("Updated sprite position from server - X: %.2f, Y: %.2f%n", sprite.getX(), sprite.getY());
                        SwingUtilities.invokeLater(() -> repaint());
                    }
                    else {
                        System.out.println("Unexpected object type received.");
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
    
        //draw the sprite
        int spriteScreenX = getWidth() / 2;
        int spriteScreenY = getHeight() / 2;
        g.fillOval(spriteScreenX - 5, spriteScreenY - 5, 10, 10); //draw the sprite as a 10x10 oval
    
        //draw particles relative to the sprite's position
        if (particles != null) {
            for (Particle p : particles) {
                double dx = p.getX() - sprite.getX();
                double dy = p.getY() - sprite.getY();
                int screenX = (int) (spriteScreenX + dx);
                int screenY = (int) (spriteScreenY + dy);
    
                //check if particles are within the bounds of the panel
                if (screenX >= 0 && screenX < getWidth() && screenY >= 0 && screenY < getHeight()) {
                    g.fillOval(screenX - 2, screenY - 2, 5, 5); //draw particles as 5x5 ovals
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
