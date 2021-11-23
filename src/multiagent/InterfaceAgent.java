package multiagent;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
					addBehaviour(new InterfaceBehaviour());
				}
			}
		});
	}

	private class InterfaceBehaviour extends SimpleBehaviour {

		private InterfaceAgent myAgent;
		private boolean finished = false;
		private int stateCounter = 0;
		private boolean isImgPathReady = false;
		private String imgPath = "none";
		private String nutriDataString = "nothing yet";

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
				// wait for input from swingUI
				if (isImgPathReady) {
					stateCounter = 1;
					isImgPathReady = false;
				}
				break;
			case 1:
				// send image file path from swingUI to CameraAgent
				sendMsg(imgPath, Constants.ImageSend, ACLMessage.INFORM, myAgent.cameraAgents);
				System.out.println(getLocalName() + " case1 - Image path sent to CameraAgent");
				stateCounter = 2;
				break;
			case 2:
				// wait for processing and reply from NutritionAgent
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId(Constants.ImageSend));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					System.out.println(getLocalName() + " case2 - received reply from NutritionAgent");
					nutriDataString = msg.getContent();
					stateCounter = 3;
				}
				break;
			case 3:
				// send back calorie data from NutritionAgent to swingUI
				System.out.println(getLocalName() + " case3 - Calorie data sent to swingUI");
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
			private JLabel label;
			private JFileChooser fileChooser;
			private JTextField textField;

			public SwingMain() {
				frame = new JFrame();
				panel = new JPanel();
				panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
				panel.setLayout(new GridLayout(0, 1));

				label = new JLabel("Pick an Image file");
				panel.add(label);

				button = new JButton("Browse");
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
				textField = new JTextField(30);
				textField.setText("none");
				panel.add(textField);

				button = new JButton("Send");
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						if (textField.getText() != "none" || textField.getText().length() > 4) {
							imgPath = textField.getText();
							isImgPathReady = true;
						} else
							System.out.println("No file was selected");
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
