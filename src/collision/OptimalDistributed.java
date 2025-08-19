package collision;

import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

public class OptimalDistributed extends JFrame {
    public OptimalDistributed() {
        JPanel panel = new JPanel(new GridBagLayout());
        this.add(panel);

        JLabel label = new JLabel("Run distributed version without graphics?");
        JButton yes = new JButton("Yes");
        JButton no = new JButton("No");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        panel.add(label, gbc);
        gbc.gridy = 1;
        panel.add(yes, gbc);
        gbc.gridy = 2;
        panel.add(no, gbc);

        //GUI Mode
        no.addActionListener(e -> {
            try {
                new DistributedMain();
                this.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error launching GUI version", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });



        yes.addActionListener(e -> {
            try {
                this.dispose();
                Scanner sc = new Scanner(System.in);
                System.out.println("Enter number of particles: ");
                int n = sc.nextInt();
                System.out.println("Enter number of cycles: ");
                int cycles = sc.nextInt();
                //Change parameter after -np to change number of JVM
                //Change the path to run on your machine, I put MPJ Express in C:
                String[] cmd = {
                        "java",
                        "-jar", "C:\\mpj\\lib\\starter.jar",
                        "-np", "16",
                        "-cp", "C:\\Users\\User\\Desktop\\CollisionParallelFix\\out\\production\\CollisionParallelFix;C:\\mpj\\lib\\mpj.jar",
                        "collision.DistributedGui",
                        "0", "C:\\mpj\\conf\\mpjexpress.conf", "multicore",
                        String.valueOf(n),
                        String.valueOf(cycles),
                        "false"  // no GUI
                };

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.inheritIO();
                pb.start();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setSize(500, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
}
