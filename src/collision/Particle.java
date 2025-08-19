package collision;
import java.io.Serializable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Particle implements Serializable {
    double x;
    double y;
    public double radius;
    int id;
    public double charge;
    private double dx;
    private double dy;
    double mass;


    Particle(int id, double x, double y, double velocity, double radius, Color color, double charge) {
        Random r = new Random(); //Add seed to create same random
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.charge = charge;
        this.id = id;
        this.mass = radius * radius * Math.PI; //Mass proportional to area

        //Generate random velocity direction
        double angle = 2 * Math.PI * r.nextDouble();
        double speed = r.nextDouble(-velocity, velocity); //Random speed
        this.dx = speed * Math.cos(angle);
        this.dy = speed * Math.sin(angle);
    }


    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public static ArrayList<Particle> generate(int count) {
        ArrayList<Particle> particles = new ArrayList<>();
        Random r = new Random();//Put seed to make testing

        for (int i = 0; i < count; i++) {
            int x = r.nextInt(750);
            int y = r.nextInt(550);
            double radius = 6;
            int charge = r.nextInt(-5, 6) + 1;
            Color color = charge >= 0 ? Color.BLUE : Color.RED;
            particles.add(new Particle(i, x, y, 3.0, radius, color, charge));
        }
        return particles;
    }

    public static double distance(Particle a, Particle b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double getCharge() {
        return charge;
    }

    public double getMass() {
        return mass;
    }


}