package collision;
import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

public class Optimal extends JFrame {
    public Optimal() {

        JPanel panel = new JPanel(new GridBagLayout());
        this.add(panel);


        JLabel label = new JLabel("Do you want to run the program\n "+
                "without the Graphics for optimal performance?");
        JButton yes = new JButton("Yes");
        JButton no = new JButton("No");


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;


        panel.add(label, gbc);


        gbc.gridy = 1;
        panel.add(yes, gbc);

        gbc.gridy = 2;
        panel.add(no, gbc);

        no.addActionListener(e -> {
            try{
                Main main = new Main();
                this.dispose();
                this.setVisible(false);
            }catch(Exception ex){
                throw new RuntimeException(ex);
            }
        });

        yes.addActionListener(e ->{
            try {
                this.dispose();
                this.setVisible(false);
                Scanner sc = new Scanner(System.in);
                System.out.println("Enter number of particles: ");
                int n = sc.nextInt();
                System.out.println("Enter number of cycles: ");
                int cycle = sc.nextInt();
                Gui gui = new Gui(n, cycle, false);

            }catch(Exception ex){
                throw new RuntimeException(ex);
            }
        });

        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setSize(500, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
}
