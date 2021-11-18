package swingui;

import javax.swing.*;
import java.awt.GridLayout;
import java.awt.BorderLayout;

public class SwingMain {

	public SwingMain() {
		JFrame frame = new JFrame();

		JButton button = new JButton("Click Me");

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
		panel.setLayout(new GridLayout(0, 1));
		panel.add(button);

		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Swing GUI");
		frame.pack();
		frame.setVisible(true);

	}

}
