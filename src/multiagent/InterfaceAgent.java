package multiagent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Constants;

public class InterfaceAgent extends ServiceAgent {

	public Set<AID> cameraAgents = new HashSet<>();
	public Set<AID> nutritionAgents = new HashSet<>();

	@Override
	protected void setup() {
		register(Constants.InterfaceService);
		addBehaviour(new TickerBehaviour(this, 2000) {
			protected void onTick() {
				cameraAgents = searchForService(Constants.CameraService);
				nutritionAgents = searchForService(Constants.NutritionService);
				if (cameraAgents.size() > 0 && nutritionAgents.size() > 0) {
					stop();
				}
				addBehaviour(new InterfaceBehaviour());
			}
		});
	}

	private class InterfaceBehaviour extends SimpleBehaviour {

		private InterfaceAgent myAgent;
		private boolean finished = false;
		private int stateCounter = 0;
		private boolean isImgPathReady = false;
		private String imgPath = "none";
		private JLabel label;
		private String nutriDataString = "Pick an Image file";

		public InterfaceBehaviour() {
			super(InterfaceAgent.this);
			myAgent = InterfaceAgent.this;
			new SwingMain();
			System.out.println(getLocalName() + " case0 - Waiting for input from SwingUI");
		}

		@Override
		public void action() {
			ACLMessage msg, reply;
			MessageTemplate template;

			switch (stateCounter) {
			case 0:
				// waiting for input from swingUI
				if (isImgPathReady) {
					stateCounter = 1;
					isImgPathReady = false;
				}
				break;
			case 1:
				// send image file path from swingUI to CameraAgent
				sendMsg(imgPath, Constants.ImageSend, ACLMessage.INFORM, myAgent.cameraAgents);
				System.out.println(getLocalName() + " case1 - Image path sent to CameraAgent");
				label.setText("Loading...");
				stateCounter = 2;
				break;
			case 2:
				// wait for processing and calorie reply from NutritionAgent
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId(Constants.CalorieSend));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					System.out.println(getLocalName() + " case2 - received calories from NutritionAgent");
					nutriDataString = msg.getContent();
					System.out.println("Calories: " + nutriDataString);
					label.setText(nutriDataString + " kcal");
					stateCounter = 3;
				}
				break;
			case 3:
				// send back calorie data from NutritionAgent to swingUI
				System.out.println(getLocalName() + " case3 - Calorie data sent to swingUI");
				stateCounter = 0;
				break;
			default:
				System.out.println(getLocalName() + " caseError - Invalid state. Resetting to 0");
				stateCounter = 0;
				break;
			}
		}

		private void sendMsg(String content, String conversationId, int type, Set<AID> receivers) {
			ACLMessage msg = new ACLMessage(type);
			msg.setContent(content);
			msg.setConversationId(conversationId);
			// add receivers
			for (AID agent : receivers) {
				msg.addReceiver(agent);
			}
			myAgent.send(msg);
		}

		@Override
		public boolean done() {
			return finished;
		}

		public class SwingMain {

			private JFrame frame;
			private JPanel panel;
			private JButton button;

			private JFileChooser fileChooser;
			private JTextField textField;

			public SwingMain() {
				frame = new JFrame();
				panel = new JPanel();
				panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
				panel.setLayout(new GridLayout(0, 1));

				label = new JLabel("Pick an Image file");
				label.setFont(new Font("Calibri", Font.BOLD, 20));
				panel.add(label);
				panel.add(new JLabel(""));
				// panel.add(new JLabel("Pick an Image file"));

				button = new JButton("Browse");
				button.setBackground(Color.WHITE);
				button.setForeground(new Color(80, 155, 200));

				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						if (fileChooser.showOpenDialog(button) == JFileChooser.APPROVE_OPTION) {
							textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
							// imgPath = textField.getText();
							// isImgPathReady = true;
						} else
							System.out.println("No file was selected");
					}
				});
				panel.add(button);

				fileChooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "jpeg");
				fileChooser.setFileFilter(filter);
				textField = new JTextField(30);
				textField.setText("none");
				panel.add(textField);

				button = new JButton("Send >");
				button.setBackground(Color.WHITE);
				button.setForeground(new Color(80, 155, 200));
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						System.out.println(textField.getText().length());
						if (textField.getText().compareTo("none") != 0 && textField.getText().length() > 4) {
							Boolean b = new File(textField.getText()).exists();
							System.out.println(b);
							if (b) {
								imgPath = textField.getText();
								isImgPathReady = true;
							} else {
								label.setText("Error: File path does not exist!");
								System.out.println("No file was selected");
							}
						} else {
							label.setText("Error: File path is invalid!");
							System.out.println("No file was selected");
						}
					}
				});
				panel.add(new JLabel(""));
				panel.add(button);
				panel.add(new JLabel(""));

				frame.add(panel, BorderLayout.CENTER);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setTitle("NutriVision - Agent-based Calorie Counter");
				frame.pack();
				frame.setVisible(true);

			}
		}
	}

}
