package dev.maheshbabu11.htmxwithjava;

    
import javax.swing.*;
import org.springframework.stereotype.Component;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Component
public class TextAreaExample extends JFrame implements ActionListener {

    private JTextArea textArea;
    private JTextArea textArea2;
    private JButton button;

    public TextAreaExample() {
        setTitle("Text Area Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 300);
        setLayout(new BorderLayout());
        //setLayout(new FlowLayout());
        //

        JPanel panel = new JPanel();


        textArea2 = new JTextArea(10, 20);
        JScrollPane scrollPane2 = new JScrollPane(textArea2);
        //add(scrollPane2, BorderLayout.SOUTH);
        //add(scrollPane2);


        button = new JButton("Append Text");
        button.addActionListener(this);
        //add(button, BorderLayout.CENTER);
        //add(button);

        textArea = new JTextArea(10, 20);
        JScrollPane scrollPane = new JScrollPane(textArea);
        //add(scrollPane, BorderLayout.NORTH);
        //add(scrollPane);
        
        panel.add(textArea);
        panel.add(button);
        panel.add(textArea2);
        
        add(panel);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            textArea.append("Button clicked!\n");
            System.out.println(textArea.getText());
            textArea2.append(textArea.getText());
        }
    }

    public static void main1(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TextAreaExample();
            }
        });
    }
}