package multiagent;

import java.awt.image.BufferedImage;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Constants;

public class ClassifierAgent extends ServiceAgent {

	@Override
	protected void setup() {
		register(Constants.ClassifierService);
		addBehaviour(new ClassifierBehaviour());
	}

	private class ClassifierBehaviour extends SimpleBehaviour {

		private boolean finished = false;
		private int stateCounter = 0;

		public ClassifierBehaviour() {
			super(ClassifierAgent.this);
			myAgent = ClassifierAgent.this;
		}

		@Override
		public void action() {
			ACLMessage msg, reply;
			MessageTemplate template;

			switch (stateCounter) {
			case 0:
				// listening for image base64 from CameraAgent
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
						MessageTemplate.MatchConversationId(Constants.ImageSend));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					System.out.println(getLocalName() + " received an image");
					System.out.println("This image is: " + msg.getContent());
					stateCounter = 1;
				}
				break;
			case 1:
				// send GatewayAgent the base64 to request label
				stateCounter = 2;
				break;
			case 2:
				// wait for label from Gateway
				stateCounter = 3;
				break;
			case 3:
				// send NutritionAgent the label
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

		@Override
		public boolean done() {
			return finished;
		}

	}

}
