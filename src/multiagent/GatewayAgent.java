package multiagent;

import java.util.HashSet;
import java.util.Set;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Constants;

public class GatewayAgent extends ServiceAgent {

	public Set<AID> classifierAgents = new HashSet<>();// set that contains a list of agents
	public Set<AID> nutritionAgents = new HashSet<>();
	
	@Override
	protected void setup() {
		register(Constants.GatewayService);
		addBehaviour(new TickerBehaviour(this, 2000) {
			protected void onTick() {
				classifierAgents = searchForService(Constants.ClassifierService);
				nutritionAgents = searchForService(Constants.NutritionService);
				if (classifierAgents.size() > 0 && nutritionAgents.size() > 0) {
					stop();
					addBehaviour(new GatewayBehaviour());
				}
			}
		});
	}

	private class GatewayBehaviour extends SimpleBehaviour {
		private GatewayAgent myAgent;
		private boolean finished = false;
		private int stateCounter = 0;
		private String base64img; 
		private String label;
		private String calories;

		public GatewayBehaviour() {
			super(GatewayAgent.this);
			myAgent = GatewayAgent.this;
		}

		@Override
		public void action() {
			ACLMessage msg, reply;
			MessageTemplate template;

			switch (stateCounter) {
			case 0:
				//listening for base64img from classifier
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId(Constants.Base64Send));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					base64img = msg.getContent();
					System.out.println(getLocalName() + " received the base64 image from Classifier:\n " + base64img);
					stateCounter = 1;
				}
				break;
			case 1:
				//API call for label then send to classifier
				//might cause asynchronous threads issue?
				break;
			case 2:
				//listening for label from nutrition
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId(Constants.LabelSend));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					System.out.println(getLocalName() + " received the label from Nutrition");
					label = msg.getContent();
					stateCounter = 3;
				}
				break;
			case 3:
				//API call for calories
				break;
			case 4:
				//send nutrition the calories
				finished = true;
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
