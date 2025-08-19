package collision;

import mpi.*;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;

public class DistributedGui extends JPanel {
    private CopyOnWriteArrayList<Particle> particles;
    private final double SOFTENING = 2.0;
    private final double restitution = 0.8;
    private int cycles;
    private boolean isShown;
    private int rank, size;
    private static final int FIELDS = 6;

    public DistributedGui(int n, int cycles, boolean isShown) throws MPIException {
        this.cycles = cycles;
        this.isShown = isShown;

        rank = MPI.COMM_WORLD.Rank();
        size = MPI.COMM_WORLD.Size();


        if (rank == 0 && isShown) {
            particles = new CopyOnWriteArrayList<>();
            JFrame frame = new JFrame("Particles - Distributed");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600); // initial size, can resize
            frame.setLocationRelativeTo(null);
            frame.add(this);
            frame.setVisible(true);
        }

        if (isShown) {
            runGuiSimulation(n);
        } else {
            runOptimizedSimulation(n);
        }
    }

    private void runOptimizedSimulation(int n) throws MPIException {
        long startTime = 0;
        if (rank == 0) startTime = System.currentTimeMillis();

        int FIELDS = 6;
        double[] flatCurrent = new double[n * FIELDS];

        if (rank == 0) {
            Random rand = new Random();
            for (int i = 0; i < n; i++) {
                int x = rand.nextInt(750);
                int y = rand.nextInt(550);
                double radius = 6;
                double velocity = 3.0;
                int charge = rand.nextInt(-5, 6) + 1;
                Color color = charge >= 0 ? Color.BLUE : Color.RED;
                Particle p = new Particle(i, x, y, velocity, radius, color, charge);
                flatCurrent[i * FIELDS + 0] = p.x;
                flatCurrent[i * FIELDS + 1] = p.y;
                flatCurrent[i * FIELDS + 2] = p.getDx();
                flatCurrent[i * FIELDS + 3] = p.getDy();
                flatCurrent[i * FIELDS + 4] = p.getMass();
                flatCurrent[i * FIELDS + 5] = p.getCharge();
            }
        }

        MPI.COMM_WORLD.Bcast(flatCurrent, 0, n * FIELDS, MPI.DOUBLE, 0);

        int chunkSize = (n + size - 1) / size;
        int start = rank * chunkSize;
        int end = Math.min(start + chunkSize, n);
        int localCount = end - start;

        //Setup for Allgatherv
        int[] sendCounts = new int[size];
        int[] displs = new int[size];
        for (int i = 0; i < size; i++) {
            int s = i * chunkSize;
            int e = Math.min(s + chunkSize, n);
            sendCounts[i] = (e - s) * FIELDS;
            displs[i] = s * FIELDS;
        }

        double[] localUpdate = new double[localCount * FIELDS];

        for (int step = 0; step < cycles; step++) {
            int panelWidth = 800, panelHeight = 600;


            for (int i = start; i < end; i++) {
                double xi = flatCurrent[i * FIELDS];
                double yi = flatCurrent[i * FIELDS + 1];
                double dxi = flatCurrent[i * FIELDS + 2];
                double dyi = flatCurrent[i * FIELDS + 3];
                double mi = flatCurrent[i * FIELDS + 4];
                double qi = flatCurrent[i * FIELDS + 5];

                for (int j = i + 1; j < n; j++) {
                    if (i == j) continue;
                    double xj = flatCurrent[j * FIELDS];
                    double yj = flatCurrent[j * FIELDS + 1];
                    double qj = flatCurrent[j * FIELDS + 5];
                    double dx = xj - xi;
                    double dy = yj - yi;
                    double distSq = dx * dx + dy * dy + SOFTENING;
                    double dist = Math.sqrt(distSq);
                    if (dist > 200) continue;
                    double invDist3 = 1.0 / (distSq * dist);
                    double f = 200.0 * qi * qj * invDist3;
                    double fx = f * dx / dist;
                    double fy = f * dy / dist;
                    dxi += fx / mi;
                    dyi += fy / mi;
                    if (dist < 12.0) {
                        double nx = dx / dist;
                        double ny = dy / dist;
                        double overlap = 12.0 - dist;
                        xi -= nx * (overlap / 2.0);
                        yi -= ny * (overlap / 2.0);
                    }
                }
                xi += dxi;
                yi += dyi;


                if (xi - 5 <= 0 || xi + 5 >= panelWidth) {
                    dxi = -dxi * restitution;
                    xi = Math.max(5.0, Math.min(panelWidth - 5.0, xi));
                }
                if (yi - 5 <= 0 || yi + 5 >= panelHeight) {
                    dyi = -dyi * restitution;
                    yi = Math.max(5.0, Math.min(panelHeight - 5.0, yi));
                }
                int localIdx = i - start;
                localUpdate[localIdx * FIELDS + 0] = xi;
                localUpdate[localIdx * FIELDS + 1] = yi;
                localUpdate[localIdx * FIELDS + 2] = dxi;
                localUpdate[localIdx * FIELDS + 3] = dyi;
                localUpdate[localIdx * FIELDS + 4] = mi;
                localUpdate[localIdx * FIELDS + 5] = qi;
            }

            MPI.COMM_WORLD.Allgatherv(
                    localUpdate, 0, localCount * FIELDS, MPI.DOUBLE,
                    flatCurrent, 0, sendCounts, displs, MPI.DOUBLE
            );
        }

        if (rank == 0) {
            long endTime = System.currentTimeMillis();
            System.out.println("Optimized distributed simulation finished in " + (endTime - startTime) + " ms");
        }

        MPI.Finalize();
    }


    private void runGuiSimulation(int n) throws MPIException {
        long startTime = 0;
        if (rank == 0) startTime = System.currentTimeMillis();

        double[] flatCurrent = new double[n * FIELDS];

        if (rank == 0) {
            Random rand = new Random(42);
            particles.clear();
            for (int i = 0; i < n; i++) {
                int x = rand.nextInt(750);
                int y = rand.nextInt(550);
                double radius = 6;
                double velocity = 3.0;
                int charge = rand.nextInt(-5, 6) + 1;
                Color color = charge >= 0 ? Color.BLUE : Color.RED;
                Particle p = new Particle(i, x, y, velocity, radius, color, charge);
                flatCurrent[i * FIELDS + 0] = p.x;
                flatCurrent[i * FIELDS + 1] = p.y;
                flatCurrent[i * FIELDS + 2] = p.getDx();
                flatCurrent[i * FIELDS + 3] = p.getDy();
                flatCurrent[i * FIELDS + 4] = p.getMass();
                flatCurrent[i * FIELDS + 5] = p.getCharge();
                particles.add(p);
            }
        }
        MPI.COMM_WORLD.Bcast(flatCurrent, 0, n * FIELDS, MPI.DOUBLE, 0);

        int chunkSize = (n + size - 1) / size;
        int start = rank * chunkSize;
        int end = Math.min(start + chunkSize, n);
        int localCount = end - start;


        int[] sendCounts = new int[size];
        int[] displs = new int[size];
        for (int i = 0; i < size; i++) {
            int s = i * chunkSize;
            int e = Math.min(s + chunkSize, n);
            sendCounts[i] = (e - s) * FIELDS;
            displs[i] = s * FIELDS;
        }

        double[] localUpdate = new double[localCount * FIELDS];

        for (int step = 0; step < cycles; step++) {

            int panelWidth = getWidth();
            int panelHeight = getHeight();


            if (panelWidth <= 0) panelWidth = 800;
            if (panelHeight <= 0) panelHeight = 600;

            for (int i = start; i < end; i++) {
                double xi = flatCurrent[i * FIELDS];
                double yi = flatCurrent[i * FIELDS + 1];
                double dxi = flatCurrent[i * FIELDS + 2];
                double dyi = flatCurrent[i * FIELDS + 3];
                double mi = flatCurrent[i * FIELDS + 4];
                double qi = flatCurrent[i * FIELDS + 5];

                for (int j = 0; j < n; j++) {
                    if (i == j) continue;
                    double xj = flatCurrent[j * FIELDS];
                    double yj = flatCurrent[j * FIELDS + 1];
                    double qj = flatCurrent[j * FIELDS + 5];
                    double dx = xj - xi;
                    double dy = yj - yi;
                    double distSq = dx * dx + dy * dy + SOFTENING;
                    double dist = Math.sqrt(distSq);
                    if (dist > 200) continue;
                    double invDist3 = 1.0 / (distSq * dist);
                    double f = 200.0 * qi * qj * invDist3;
                    double fx = f * dx / dist;
                    double fy = f * dy / dist;
                    dxi += fx / mi;
                    dyi += fy / mi;
                }
                xi += dxi;
                yi += dyi;

                if (xi - 5 <= 0 || xi + 5 >= panelWidth) {
                    dxi = -dxi * restitution;
                    xi = Math.max(5.0, Math.min(panelWidth - 5.0, xi));
                }
                if (yi - 5 <= 0 || yi + 5 >= panelHeight) {
                    dyi = -dyi * restitution;
                    yi = Math.max(5.0, Math.min(panelHeight - 5.0, yi));
                }
                int localIdx = i - start;
                localUpdate[localIdx * FIELDS + 0] = xi;
                localUpdate[localIdx * FIELDS + 1] = yi;
                localUpdate[localIdx * FIELDS + 2] = dxi;
                localUpdate[localIdx * FIELDS + 3] = dyi;
                localUpdate[localIdx * FIELDS + 4] = mi;
                localUpdate[localIdx * FIELDS + 5] = qi;
            }

            MPI.COMM_WORLD.Allgatherv(
                    localUpdate, 0, localCount * FIELDS, MPI.DOUBLE,
                    flatCurrent, 0, sendCounts, displs, MPI.DOUBLE
            );


            if (rank == 0) {
                for (int i = 0; i < n; i++) {
                    for (int j = i + 1; j < n; j++) {
                        double xi = flatCurrent[i * FIELDS];
                        double yi = flatCurrent[i * FIELDS + 1];
                        double dxi = flatCurrent[i * FIELDS + 2];
                        double dyi = flatCurrent[i * FIELDS + 3];
                        double mi = flatCurrent[i * FIELDS + 4];

                        double xj = flatCurrent[j * FIELDS];
                        double yj = flatCurrent[j * FIELDS + 1];
                        double dxj = flatCurrent[j * FIELDS + 2];
                        double dyj = flatCurrent[j * FIELDS + 3];
                        double mj = flatCurrent[j * FIELDS + 4];

                        double dx = xj - xi;
                        double dy = yj - yi;
                        double dist = Math.sqrt(dx * dx + dy * dy + SOFTENING);

                        if (dist < 12.0) {
                            double nx = dx / dist;
                            double ny = dy / dist;
                            double overlap = 12.0 - dist;


                            flatCurrent[i * FIELDS + 0] -= nx * (overlap / 2.0);
                            flatCurrent[i * FIELDS + 1] -= ny * (overlap / 2.0);
                            flatCurrent[j * FIELDS + 0] += nx * (overlap / 2.0);
                            flatCurrent[j * FIELDS + 1] += ny * (overlap / 2.0);


                            double dvx = dxj - dxi;
                            double dvy = dyj - dyi;
                            double relVel = dvx * nx + dvy * ny;
                            if (relVel > 0) continue;
                            double impulse = -(1 + restitution) * relVel;
                            impulse /= (1.0 / mi + 1.0 / mj);
                            double impulseX = impulse * nx;
                            double impulseY = impulse * ny;

                            flatCurrent[i * FIELDS + 2] -= impulseX / mi;
                            flatCurrent[i * FIELDS + 3] -= impulseY / mi;
                            flatCurrent[j * FIELDS + 2] += impulseX / mj;
                            flatCurrent[j * FIELDS + 3] += impulseY / mj;
                        }
                    }
                }
            }
            MPI.COMM_WORLD.Bcast(flatCurrent, 0, n * FIELDS, MPI.DOUBLE, 0);

            //GUI update (only on rank 0)
            if (rank == 0) {
                SwingUtilities.invokeLater(() -> {
                    particles.clear();
                    for (int i = 0; i < n; i++) {
                        double x = flatCurrent[i * FIELDS];
                        double y = flatCurrent[i * FIELDS + 1];
                        double dx = flatCurrent[i * FIELDS + 2];
                        double dy = flatCurrent[i * FIELDS + 3];
                        double mass = flatCurrent[i * FIELDS + 4];
                        double charge = flatCurrent[i * FIELDS + 5];
                        Color color = charge >= 0 ? Color.BLUE : Color.RED;
                        Particle p = new Particle(i, x, y, 3.0, 6, color, charge);
                        p.setDx(dx);
                        p.setDy(dy);
                        particles.add(p);
                    }
                    repaint();
                });
                try { Thread.sleep(16); } catch (InterruptedException ignored) {}
            }
        }
        if (rank == 0) {
            long endTime = System.currentTimeMillis();
            System.out.println("Optimized distributed simulation finished in " + (endTime - startTime) + " ms");
            SwingUtilities.invokeLater(() -> {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) window.dispose();
            });
        }
        MPI.Finalize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.WHITE);
        if (particles == null) return;
        Graphics2D gfx = (Graphics2D) g;
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Particle p : particles) {
            gfx.setColor(p.getCharge() >= 1 ? Color.BLUE : Color.RED);
            double x = p.getX();
            double y = p.getY();
            gfx.fillOval((int) (x - p.radius), (int) (y - p.radius),
                    (int) (2 * p.radius), (int) (2 * p.radius));
        }
    }

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int n = Integer.parseInt(args[6]);
        int cycles = Integer.parseInt(args[7]);
        boolean show = Boolean.parseBoolean(args[8]);
        new DistributedGui(n, cycles, show);
    }
}
