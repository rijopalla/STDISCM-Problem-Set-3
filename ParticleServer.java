import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;


public class ParticleServer extends JPanel {
    public Canvas canvas;
    private JTextField startXField, startYField, velocityField, startThetaField;
    private JButton addButton;
    private JRadioButton batchOption1, batchOption2, batchOption3;
    private ServerSocket serverSocket;
    public List<ClientHandler> clientHandlers;

    public ParticleServer() {
        setLayout(new BorderLayout());
        clientHandlers = new CopyOnWriteArrayList<>();
        setupUI();
        
        //start server
        startServer();
    }

    private void setupUI() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(6, 1));

        sidebar.add(new JLabel("Start X Position:"));
        startXField = new JTextField();
        sidebar.add(startXField);

        sidebar.add(new JLabel("Start Y Position:"));
        startYField = new JTextField();
        sidebar.add(startYField);

        sidebar.add(new JLabel("Velocity:"));
        velocityField = new JTextField();
        sidebar.add(velocityField);

        sidebar.add(new JLabel("Start Angle (degrees):"));
        startThetaField = new JTextField();
        sidebar.add(startThetaField);

        addButton = new JButton("Add Particle");
        addButton.addActionListener(e -> addParticle());

        JPanel batchPanel = new JPanel();
        batchPanel.setLayout(new BoxLayout(batchPanel, BoxLayout.Y_AXIS));

        batchOption1 = new JRadioButton("Form 1 (Start & End Point)");
        batchOption2 = new JRadioButton("Form 2 (Angle)");
        batchOption3 = new JRadioButton("Form 3 (Velocity)");

        ButtonGroup batchGroup = new ButtonGroup();
        batchGroup.add(batchOption1);
        batchGroup.add(batchOption2);
        batchGroup.add(batchOption3);

        batchPanel.add(batchOption1);
        batchPanel.add(batchOption2);
        batchPanel.add(batchOption3);

        sidebar.add(new JLabel("Adding particles in batches: "));
        sidebar.add(batchPanel);
        sidebar.add(addButton);

        batchOption1.addActionListener(e -> handleBatchOption1());
        batchOption2.addActionListener(e -> handleBatchOption2());
        batchOption3.addActionListener(e -> handleBatchOption3());

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(1280, 720));
        add(sidebar, BorderLayout.EAST);
        add(canvas, BorderLayout.CENTER);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                canvas.repaint();
                broadcastParticleStates();
            }
        }, 0, 16);
    }

    private void addParticle() {
        try {
            double startX = Double.parseDouble(startXField.getText());
            double startY = Double.parseDouble(startYField.getText());
            double velocity = Double.parseDouble(velocityField.getText());
            double startTheta = Double.parseDouble(startThetaField.getText());

            //make sure particles are within bounds
            startX = Math.min(startX, canvas.getWidth() - 5);
            startY = Math.min(startY, canvas.getHeight() - 5);

            canvas.addParticle(new Particle(startX, startY, velocity, startTheta));
            canvas.repaint();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(ParticleServer.this, "Invalid input. Please check your entries.");
        }
    }


    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(12345);
                System.out.println("Server started on port 12345.");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    clientHandlers.add(handler);
                    new Thread(handler).start();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    //batch options
    private void handleBatchOption1() { //if batch option 1 was picked
        JTextField numParticlesField = new JTextField();
        JTextField startXField = new JTextField();
        JTextField startYField = new JTextField();
        JTextField endXField = new JTextField();
        JTextField endYField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Number of Particles:"));
        panel.add(numParticlesField);
        panel.add(new JLabel("Start X Position:"));
        panel.add(startXField);
        panel.add(new JLabel("Start Y Position:"));
        panel.add(startYField);
        panel.add(new JLabel("End X Position:"));
        panel.add(endXField);
        panel.add(new JLabel("End Y Position:"));
        panel.add(endYField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Batch Particle Input", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int numParticles = Integer.parseInt(numParticlesField.getText());
                double startX = Double.parseDouble(startXField.getText());
                double startY = Double.parseDouble(startYField.getText());
                double endX = Double.parseDouble(endXField.getText());
                double endY = Double.parseDouble(endYField.getText());

                if (numParticles < 2) {
                    JOptionPane.showMessageDialog(ParticleServer.this, "Number of particles must be at least 2.");
                    return;
                }

            //calculate the total distance between start and end points
            double totalDistance = Math.hypot(endX - startX, endY - startY);
            
            //define a scaling factor to adjust the spacing between particles
            double scaleFactor = 1.0; // Adjust this value to control the spacing
             
            //calculate the scaled distance between particles
            double scaledDistance = totalDistance / ((numParticles - 1) * scaleFactor);

            //calculate the unit vector pointing from start to end
            double unitDX = (endX - startX) / totalDistance;
            double unitDY = (endY - startY) / totalDistance;

            for (int i = 0; i < numParticles; i++) {
                double progress = i / (double)(numParticles - 1);
                double particleX = startX + progress * scaledDistance * unitDX;
                double particleY = startY + progress * scaledDistance * unitDY;
                canvas.addParticle(new Particle(particleX, particleY, 1.0, 0, endX, endY));
            }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ParticleServer.this, "Invalid input. Please check your entries.");
            }
        }
    }

    private void handleBatchOption2() { //if batch option 2 was picked
        JTextField numParticlesInput = new JTextField();
        JTextField startThetaInput = new JTextField();
        JTextField endThetaInput = new JTextField();

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Number of Particles:"));
        panel.add(numParticlesInput);
        panel.add(new JLabel("Start Theta (degrees):"));
        panel.add(startThetaInput);
        panel.add(new JLabel("End Theta (degrees):"));
        panel.add(endThetaInput);

        int result = JOptionPane.showConfirmDialog(null, panel, "Batch Particle Input", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int numParticles = Integer.parseInt(numParticlesInput.getText());
                double startTheta = Math.toRadians(Double.parseDouble(startThetaInput.getText()));
                double endTheta = Math.toRadians(Double.parseDouble(endThetaInput.getText()));

                if (numParticles < 2) {
                    JOptionPane.showMessageDialog(ParticleServer.this, "Number of particles must be at least 2.");
                    return;
                }

                double dTheta = (endTheta - startTheta) / (numParticles - 1);
                //start point and end point are constant:
                double startX = 640; 
                double startY = 360;
                //velocity should also be constant:
                double velocity = 1.0;

                for (int i = 0; i < numParticles; i++) {
                    double theta = startTheta + i * dTheta;
                    canvas.addParticle(new Particle(startX, startY, velocity, Math.toDegrees(theta)));
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ParticleServer.this, "Invalid input. Please check your entries.");
            }
        }
    }

private void handleBatchOption3() { //if batch option 3 was picked
        JTextField numParticlesInput = new JTextField();
        JTextField startVelocityInput = new JTextField();
        JTextField endVelocityInput = new JTextField();

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Number of Particles:"));
        panel.add(numParticlesInput);
        panel.add(new JLabel("Start Velocity:"));
        panel.add(startVelocityInput);
        panel.add(new JLabel("End Velocity:"));
        panel.add(endVelocityInput);

        int result = JOptionPane.showConfirmDialog(null, panel, "Batch Particle Input", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int numParticles = Integer.parseInt(numParticlesInput.getText());
                double startVelocity = Double.parseDouble(startVelocityInput.getText());
                double endVelocity = Double.parseDouble(endVelocityInput.getText());

                if (numParticles < 2) {
                    JOptionPane.showMessageDialog(ParticleServer.this, "Number of particles must be at least 2.");
                    return;
                }

                double dVelocity = (endVelocity - startVelocity) / (numParticles - 1);
                double startX = 640;
                double startY = 360;
                double startTheta = 0;
                double maxVelocity = 25;
                double minVelocity = 10;

                for (int i = 0; i < numParticles; i++) {
                    double velocity = startVelocity + i * dVelocity;
                    velocity = Math.min(Math.max(velocity, minVelocity), maxVelocity);
                    Particle particle = new Particle(startX, startY, velocity, startTheta);
                    particle.setTargetVelocity(endVelocity);
                    canvas.addParticle(particle);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ParticleServer.this, "Invalid input. Please check your entries.");
            }
        }
    }

    private void broadcastParticleStates() {
        for (ClientHandler handler : clientHandlers) {
            handler.sendParticleStates(canvas.getParticles());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Particle Simulation Server");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new ParticleServer());
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}