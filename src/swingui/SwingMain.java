package swingui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SwingMain implements ActionListener {

	int count = 0;
	private JFrame frame;
	private JButton button;
	private JPanel panel;
	private JLabel label;

	public SwingMain() {
		frame = new JFrame();

		button = new JButton("Click Me");
		button.addActionListener(this);

		panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
		panel.setLayout(new GridLayout(0, 1));
		panel.add(button);

		label = new JLabel("number of clicks: 0");
		panel.add(label);

		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Swing GUI");
		frame.pack();
		frame.setVisible(true);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		count++;
		label.setText("number of clicks: " + count);

	}

}
