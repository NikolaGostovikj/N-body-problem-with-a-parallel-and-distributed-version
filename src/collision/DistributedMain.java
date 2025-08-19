package collision;

import javax.swing.*;
import java.awt.*;

public class DistributedMain {
    public DistributedMain() {
        JFrame frame = new JFrame("Distributed Particle Simulator");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel label = new JLabel("Enter number of particles:");
        JTextField textField = new JTextField();
        JLabel label2 = new JLabel("Enter the number of cycles:");
        JTextField textField2 = new JTextField();
        JButton create = new JButton("Create");

        textField.setMaximumSize(new Dimension(200, 30));
        textField2.setMaximumSize(new Dimension(200, 30));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label2.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField2.setAlignmentX(Component.CENTER_ALIGNMENT);
        create.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(textField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(label2);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(textField2);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(create);

        frame.add(panel);

        create.addActionListener(e -> {
            try {
                int n = Integer.parseInt(textField.getText());
                int cycles = Integer.parseInt(textField2.getText());

                //Build the MPJ Express command
                String[] cmd = {
                        "java",
                        "-jar", "C:\\mpj\\lib\\starter.jar",
                        "-np", "16",
                        "-cp", "C:\\Users\\User\\Desktop\\CollisionParallelFix\\out\\production\\CollisionParallelFix;C:\\mpj\\lib\\mpj.jar",
                        "collision.DistributedGui",
                        "0", "C:\\mpj\\conf\\mpjexpress.conf", "multicore",
                        String.valueOf(n),
                        String.valueOf(cycles),
                        "true"
                };


                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.inheritIO();
                pb.start();

                frame.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error launching MPI process!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }
}
