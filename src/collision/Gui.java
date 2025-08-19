
package collision;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static collision.Particle.distance;
import static collision.Particle.generate;

public class Gui extends JPanel {
    private ArrayList<Particle> particles;
    private int cycles;
    private int currentCycle = 0;
    private boolean isShown;
    private Timer timer;

    public Gui(int n, int cycles, boolean isShown) {
        this.particles = generate(n);
        this.cycles = cycles;
        this.isShown = isShown;

        JFrame frame = new JFrame("Particles - Sequential");

        if (isShown) {

            JSlider addSlider = new JSlider(0, 100, 0);
            addSlider.setMajorTickSpacing(20);
            addSlider.setPaintTicks(true);
            addSlider.setPaintLabels(true);

            JLabel sliderLabel = new JLabel("Add Particles:");
            JButton addButton = new JButton("Add");


            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new FlowLayout());
            controlPanel.add(sliderLabel);
            controlPanel.add(addSlider);
            controlPanel.add(addButton);

            frame.add(controlPanel, BorderLayout.SOUTH);


            addButton.addActionListener(e -> {
                int numToAdd = addSlider.getValue();
                if (numToAdd > 0) {
                    ArrayList<Particle> newParticles = Particle.generate(numToAdd);
                    particles.addAll(newParticles);
                    addSlider.setValue(0); // reset
                }
            });
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setSize(800, 600);
            frame.add(this);
            frame.setResizable(true);
            frame.setVisible(true);
        }

        startSimulation(frame);
    }



    private void startSimulation(JFrame frame) {
        long start = System.currentTimeMillis();

        if (!isShown) {
            for (int i = 0; i < cycles; i++) {
                updatePosition(particles);
                currentCycle++;
            }
            long end = System.currentTimeMillis();
            System.out.println("The Simulation has been successful!\n " +
                    "Run time in ms: " + (end - start) + "\n" +
                    "Cycles passed: " + currentCycle);

            System.exit(0);
            return;
        } else {
            timer = new Timer(1000 / 60, e -> {
                if (currentCycle >= cycles) {
                    timer.stop();
                    long end = System.currentTimeMillis();
                    JOptionPane.showMessageDialog(frame, "The Simulation has been successful!\n " +
                            "Run time in ms: " + (end - start) + "\n" +
                            "Cycles passed: " + currentCycle, "Success", JOptionPane.INFORMATION_MESSAGE);

                    System.exit(0);
                    return;
                }
                updatePosition(particles);
                repaint();
                currentCycle++;
            });
        }

        if (isShown) timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.WHITE);
        drawParticles(g);
    }

    private void drawParticles(Graphics g) {
        Graphics2D gfx = (Graphics2D) g;

        for (Particle particle : particles) {
            if (particle.getCharge() >= 1) {
                gfx.setColor(Color.BLUE);
            } else {
                gfx.setColor(Color.RED);
            }
            double x = particle.getX();
            double y = particle.getY();
            gfx.fillOval((int) (x - particle.radius), (int) (y - particle.radius),
                    (int) (2 * particle.radius), (int) (2 * particle.radius));
        }
    }

    public void updatePosition(ArrayList<Particle> particles) {
        // First, move particles and handle wall collisions
        for (Particle p : particles) {
            double newX = p.getX() + p.getDx();
            double newY = p.getY() + p.getDy();

            //Wall collision with proper bouncing
            //Add some energy loss
            if (newX - p.radius <= 0 || newX + p.radius >= getWidth()) {
                p.setDx(-p.getDx() * 0.8);
                newX = Math.max(p.radius, Math.min(getWidth() - p.radius, newX));
            }
            if (newY - p.radius <= 0 || newY + p.radius >= getHeight()) {
                p.setDy(-p.getDy() * 0.8);
                newY = Math.max(p.radius, Math.min(getHeight() - p.radius, newY));
            }


            p.x = newX;
            p.y = newY;
        }

        //Handle particle-to-particle collisions
        for (int i = 0; i < particles.size(); i++) {
            Particle p1 = particles.get(i);
            for (int j = i + 1; j < particles.size(); j++) {
                Particle p2 = particles.get(j);
                double dist = distance(p1, p2);

                if (dist > 0 && dist < 200) { //Avoid division by zero
                    //Stariot collision test, proveri dali e >0 i radius okolu particles
                    // Calculate electrostatic force using Coulomb's law
                    // F = k * q1 * q2 / d^2
                   /* double k = 200;
                    double force = k * p1.charge * p2.charge / (dist * dist);
                    double dx = p2.x - p1.x;
                    double dy = p2.y - p1.y;
                    double unitX = dx / dist;
                    double unitY = dy / dist;
                    double forceX = force * unitX;
                    double forceY = force * unitY;
                    */
                    double k = 200.0;
                    double SOFTENING = 2.0; //Prevents force from exploding at very close range

                    double dx = p1.x - p2.x;
                    double dy = p1.y - p2.y; //ne koristam funkcija deka mora da vratam i dx i dy i dist^2
                    double dist2 = dx * dx + dy * dy + SOFTENING;
                    double distInv = Math.sqrt(dist2);
                    double invDist3 = 1.0 / (dist2 * distInv);

                    //Edinechen vektor od p2 do p1
                    double ux = dx / distInv;
                    double uy = dy / distInv;
                    double f = k * p1.charge * p2.charge * invDist3;

                    double fx = f * ux;
                    double fy = f * uy;

                    //F = ma znachi a = F/m
                    p1.setDx(p1.getDx() + fx / p1.getMass());
                    p1.setDy(p1.getDy() + fy / p1.getMass());
                    p2.setDx(p2.getDx() - fx / p2.getMass());
                    p2.setDy(p2.getDy() - fy / p2.getMass());
                    /*
                    if (force > 0) { //Same charges
                        p1.setDx(p1.getDx() - forceX / p1.getMass());
                        p1.setDy(p1.getDy() - forceY / p1.getMass());
                        p2.setDx(p2.getDx() + forceX / p2.getMass());
                        p2.setDy(p2.getDy() + forceY / p2.getMass());
                    } else { //Opposite charges
                        p1.setDx(p1.getDx() + forceX / p1.getMass());
                        p1.setDy(p1.getDy() + forceY / p1.getMass());
                        p2.setDx(p2.getDx() - forceX / p2.getMass());
                        p2.setDy(p2.getDy() - forceY / p2.getMass());
                    }
                    */


                    //Ova e logika za overlap, preku formula za circles
                    if (dist <= p1.radius + p2.radius && dist > 0) {

                        dx = p2.x - p1.x;
                        dy = p2.y - p1.y;
                        double normalX = dx / dist;
                        double normalY = dy / dist;


                        double overlap = p1.radius + p2.radius - dist;
                        double separationX = normalX * (overlap / 2.0);
                        double separationY = normalY * (overlap / 2.0);

                        p1.x -= separationX;
                        p1.y -= separationY;
                        p2.x += separationX;
                        p2.y += separationY;


                        double relativeVelX = p2.getDx() - p1.getDx();
                        double relativeVelY = p2.getDy() - p1.getDy();


                        double relativeVelAlongNormal = relativeVelX * normalX + relativeVelY * normalY;


                        if (relativeVelAlongNormal > 0) {
                            continue;
                        }

                        //impuls i masa za particles za da bide realistichno
                        double restitution = 0.8;
                        double impulseScalar = -(1 + restitution) * relativeVelAlongNormal;
                        impulseScalar /= (1.0 / p1.getMass() + 1.0 / p2.getMass());


                        double impulseX = impulseScalar * normalX;
                        double impulseY = impulseScalar * normalY;

                        p1.setDx(p1.getDx() - impulseX / p1.getMass());
                        p1.setDy(p1.getDy() - impulseY / p1.getMass());
                        p2.setDx(p2.getDx() + impulseX / p2.getMass());
                        p2.setDy(p2.getDy() + impulseY / p2.getMass());
                    }
                }
            }
        }
    }


}