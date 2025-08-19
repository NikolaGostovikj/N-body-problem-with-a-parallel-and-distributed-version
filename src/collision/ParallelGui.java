package collision;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static collision.Particle.distance;

public class ParallelGui extends JPanel {

    private ArrayList<Particle> particles;
    private int cycles;
    private int currentCycle = 0;
    private boolean isShown;
    private Timer timer;
    private ExecutorService executor;
    private final int numThreads = Runtime.getRuntime().availableProcessors();
    private final double dt = 1.0 / 60.0;
    private final double k = 200.0;
    private final double SOFTENING = 2.0;
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    public ParallelGui(int n, int cycles, boolean isShown) {
        this.particles = Particle.generate(n);
        this.cycles = cycles;
        this.isShown = isShown;
        this.executor = Executors.newFixedThreadPool(numThreads);

        JFrame frame = new JFrame("Particles - Parallel");

        if (isShown) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setSize(WIDTH, HEIGHT);
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
                updatePositionParallel(particles);
                currentCycle++;
            }
            long end = System.currentTimeMillis();
            System.out.println("Parallel Simulation complete.");
            System.out.println("Run time: " + (end - start) + "ms");
            System.exit(0);
            executor.shutdown();

        } else {
            timer = new Timer(1000 / 60, e -> {
                if (currentCycle >= cycles) {
                    timer.stop();
                    long end = System.currentTimeMillis();
                    JOptionPane.showMessageDialog(frame,
                            "Parallel Simulation complete.\nTime: " + (end - start) + " ms\nCycles: " + currentCycle);
                    System.exit(0);
                }
                updatePositionParallel(particles);
                repaint();
                currentCycle++;
            });
            timer.start();
        }
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
            gfx.setColor(particle.getCharge() >= 1 ? Color.BLUE : Color.RED);
            double x = particle.getX();
            double y = particle.getY();
            gfx.fillOval((int) (x - particle.radius), (int) (y - particle.radius),
                    (int) (2 * particle.radius), (int) (2 * particle.radius));
        }
    }

    private void updatePositionParallel(List<Particle> particles) {
        int n = particles.size();

        double[][] fx = new double[numThreads][n];
        double[][] fy = new double[numThreads][n];

        int chunkSize = (n + numThreads - 1) / numThreads;

        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int tid = t;
            final int start = tid * chunkSize;
            final int end = Math.min(start + chunkSize, n);

            futures.add(executor.submit(() -> {
                for (int i = start; i < end; i++) {
                    Particle p1 = particles.get(i);
                    for (int j = 0; j < n; j++) {
                        if (i == j) continue;
                        Particle p2 = particles.get(j);
                        double distFrom = distance(p1, p2);

                        if (distFrom > 0 && distFrom < 200) {

                            double dx = p1.x - p2.x;
                            double dy = p1.y - p2.y;
                            double dist2 = dx * dx + dy * dy + SOFTENING;
                            double dist = Math.sqrt(dist2);
                            double invDist3 = 1.0 / (dist2 * dist);

                            double f = k * p1.charge * p2.charge * invDist3;
                            double fxVal = f * dx;
                            double fyVal = f * dy;

                            fx[tid][i] += fxVal;
                            fy[tid][i] += fyVal;
                        }
                    }
                }
            }));
        }

        for (Future<?> f : futures) {
            try { f.get(); } catch (Exception e) { e.printStackTrace(); }
        }


        for (int i = 0; i < n; i++) {
            double totalFx = 0.0, totalFy = 0.0;
            for (int t = 0; t < numThreads; t++) {
                totalFx += fx[t][i];
                totalFy += fy[t][i];
            }

            Particle p = particles.get(i);
            p.setDx(p.getDx() + totalFx / p.getMass());
            p.setDy(p.getDy() + totalFy / p.getMass());

            double newX = p.x + p.getDx();
            double newY = p.y + p.getDy();

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


        parallelResolveOverlaps(particles);
    }





    public void parallelResolveOverlaps(List<Particle> particles) {
        int n = particles.size();
        List<Future<?>> futures = new ArrayList<>();
        double restitution = 0.8;
        int chunkSize = (n + numThreads - 1) / numThreads;

        for (int t = 0; t < numThreads; t++) {
            final int tid = t;
            final int start = tid * chunkSize;
            final int end = Math.min(start + chunkSize, n);

            futures.add(executor.submit(() -> {
                for (int i = start; i < end; i++) {
                    Particle p1 = particles.get(i);
                    for (int j = 0; j < n; j++) {
                        if (i >= j) continue;
                        Particle p2 = particles.get(j);
                        double dist = Particle.distance(p1, p2);
                        if (dist > 0 && dist <= p1.radius + p2.radius) {
                            double dx = p2.x - p1.x;
                            double dy = p2.y - p1.y;
                            double normalX = dx / dist;
                            double normalY = dy / dist;

                            double overlap = p1.radius + p2.radius - dist;
                            double separationX = normalX * (overlap / 2.0);
                            double separationY = normalY * (overlap / 2.0);

                            //Ordered locking for thread safety
                            Particle first = (p1.id < p2.id) ? p1 : p2;
                            Particle second = (p1.id < p2.id) ? p2 : p1;
                            synchronized (first) {
                                synchronized (second) {
                                    p1.x -= separationX;
                                    p1.y -= separationY;
                                    p2.x += separationX;
                                    p2.y += separationY;

                                    double relativeVelX = p2.getDx() - p1.getDx();
                                    double relativeVelY = p2.getDy() - p1.getDy();
                                    double relativeVelAlongNormal = relativeVelX * normalX + relativeVelY * normalY;

                                    if (relativeVelAlongNormal > 0) {
                                        return;
                                    }

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
            }));
        }

        for (Future<?> f : futures) {
            try { f.get(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

}