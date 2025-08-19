package collision;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public Main(){

        JFrame frame = new JFrame("Particle Simulator");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);


        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));


        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));


        JLabel label = new JLabel("Enter number of particles:");
        label.setFont(new Font("Verdana", Font.PLAIN, 16));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField textField = new JTextField();
        textField.setMaximumSize(new Dimension(200, 30));
        textField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label2 = new JLabel("Enter the number of cycles:");
        label2.setFont(new Font("Verdana", Font.PLAIN, 16));
        label2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField textField2 = new JTextField();
        textField2.setMaximumSize(new Dimension(200, 30));
        textField2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton create = new JButton("Create");
        create.setFont(new Font("Verdana", Font.BOLD, 16));


        create.setFocusPainted(false);
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


        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String input1 = textField.getText();
                    String input2 = textField2.getText();

                    if (input1.isEmpty() || input2.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "A field is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int numParticles = Integer.parseInt(input1);
                    int cycles = Integer.parseInt(input2);

                    if (numParticles <= 0 || cycles <= 0) {
                        JOptionPane.showMessageDialog(frame, "Numbers must be greater than zero", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Gui gui = new Gui(numParticles,cycles, true);
                    frame.removeAll();
                    frame.setVisible(false);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input! Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.setVisible(true);
    }
}
