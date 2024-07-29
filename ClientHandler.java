import java.io.*;
import java.net.Socket;
import java.util.List;

import javax.swing.SwingUtilities;


public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ParticleServer server;

    public ClientHandler(Socket socket, ParticleServer server) {
        this.socket = socket;
        this.server = server;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object input = in.readObject();
                if (input instanceof Sprite) {
                    Sprite sprite = (Sprite) input;
                    System.out.printf("Received Sprite - X: %.2f, Y: %.2f%n", sprite.getX(), sprite.getY());
                    SwingUtilities.invokeLater(() -> server.canvas.updateSprite(sprite));
                    out.writeObject(sprite);
                    out.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            server.clientHandlers.remove(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendParticleStates(List<Particle> particles) {
        if (out != null) {    
            try {
                System.out.println("Sending particles: " + particles.size());
                out.reset();
                out.writeObject(particles);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Output stream is null. Cannot send data.");
        }
    }
}