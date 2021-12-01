package multiagent;

import java.util.HashSet;
import java.util.Set;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Constants;

public class ClassifierAgent extends ServiceAgent {

	public Set<AID> nutritionAgents = new HashSet<>();
	public Set<AID> gatewayAgents = new HashSet<>();

	@Override
	protected void setup() {
		register(Constants.ClassifierService);
		addBehaviour(new TickerBehaviour(this, 2000) {
			protected void onTick() {
				gatewayAgents = searchForService(Constants.GatewayService);
				nutritionAgents = searchForService(Constants.NutritionService);
				if (gatewayAgents.size() >= 0 && nutritionAgents.size() > 0) {
					stop();
					addBehaviour(new ClassifierBehaviour());
				}
			}
		});

	}

	private class ClassifierBehaviour extends SimpleBehaviour {

		private ClassifierAgent myAgent;
		private boolean finished = false;
		private int stateCounter = 0;
		private String serializedImage = null;
		private String label = null;

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
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId(Constants.Base64Send));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					System.out.println(getLocalName() + " received a serialized image from Camera");
					serializedImage = msg.getContent();
					stateCounter = 1;
				}
				break;
			case 1:
				// send GatewayAgent the base64 to get label
				sendMsg(serializedImage, Constants.Base64Send, ACLMessage.INFORM, myAgent.gatewayAgents);
				System.out.println(getLocalName() + " sent the serialized image to Gateway");
				stateCounter = 2;
				break;
			case 2:
				// wait for label from Gateway
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId(Constants.LabelSend));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					System.out.println(getLocalName() + " received the label from Gateway");
					label = msg.getContent();
					stateCounter = 3;
				}
				break;
			case 3:
				// send NutritionAgent the label
				sendMsg(label, Constants.LabelSend, ACLMessage.INFORM, myAgent.nutritionAgents);
				stateCounter = 0;
//				finished = true;
				break;
			}
		}

		private void sendMsg(String content, String conversationId, int type, Set<AID> receivers) {
			ACLMessage msg = new ACLMessage(type);
			msg.setContent(content);
			msg.setConversationId(conversationId);
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
