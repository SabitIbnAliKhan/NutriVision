package multiagent;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Constants;

public class CameraAgent extends ServiceAgent {

	public Set<AID> classifierAgents = new HashSet<>();

	@Override
	protected void setup() {
		register(Constants.CameraService);
		addBehaviour(new TickerBehaviour(this, 2000) {
			protected void onTick() {
				classifierAgents = searchForService(Constants.ClassifierService);
				if (classifierAgents.size() >= 1) {
					stop();
					addBehaviour(new CameraBehaviour());
				}
			}
		});
	}

	private class CameraBehaviour extends SimpleBehaviour {
		private CameraAgent myAgent;
		private boolean finished = false;
		private int stateCounter = 0; // changes to 0 when case 0 is taken from interfaceAgent

		// Image processing variables

		public CameraBehaviour() {
			super(CameraAgent.this);
			myAgent = CameraAgent.this;
		}

		@Override
		public void action() {
			ACLMessage msg, reply;
			MessageTemplate template;
			BufferedImage image;
			switch (stateCounter) {
			case 0:
				// listening for image input from Interface
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId(Constants.ImageSend));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					System.out.println(getLocalName() + " received an image");
					System.out.println("This image is: " + msg.getContent());
					stateCounter = 1;
				}
				// stateCounter = 1;
				break;
			case 1:
				// send ClassifierAgent the base64 converted image
				sendMsg("data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA\n"
						+ "AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO\n"
						+ "9TXL0Y4OHwAAAABJRU5ErkJggg==", Constants.Base64Send, ACLMessage.REQUEST,
						myAgent.classifierAgents);
				stateCounter = 2;
				finished = true;
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

		// add method to convert image to bin-64

		@Override
		public boolean done() {
			return finished;
		}

	}
}