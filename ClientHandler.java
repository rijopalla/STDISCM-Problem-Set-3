import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ParticleServer server;
    private Sprite sprite;

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
                    sprite = (Sprite) input;
                    System.out.printf("Received Sprite - X: %.2f, Y: %.2f%n", sprite.getX(), sprite.getY());
                    server.updateClientSprite(this, sprite);
                    server.broadcastSprites();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("An error occurred while communicating with the client.");
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public void sendParticleStates(List<Particle> particles) throws IOException {
        if (out != null) {
            try {
                System.out.println("Sending particles: " + particles.size());
                out.writeUnshared(particles);  // Send particles without shared references
                out.flush();
            } catch (IOException e) {
                throw new IOException("Failed to send particle states.", e);
            }
        } else {
            System.err.println("Output stream is null. Cannot send data.");
        }
    }

    public void sendSprites(List<Sprite> sprites) throws IOException {
        if (out != null) {
            try {
                out.writeUnshared(sprites);  // Send sprites without shared references
                out.flush();
            } catch (IOException e) {
                throw new IOException("Failed to send sprites.", e);
            }
        } else {
            System.err.println("Output stream is null. Cannot send data.");
        }
    }

    public void cleanup() {
        server.clientHandlers.remove(this);
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
