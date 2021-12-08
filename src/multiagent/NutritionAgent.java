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

public class NutritionAgent extends ServiceAgent {

	public Set<AID> interfaceAgents = new HashSet<>();
	public Set<AID> gatewayAgents = new HashSet<>();

	@Override
	protected void setup() {
		register(Constants.NutritionService);
		addBehaviour(new TickerBehaviour(this, 2000) {
			protected void onTick() {
				gatewayAgents = searchForService(Constants.GatewayService);
				interfaceAgents = searchForService(Constants.InterfaceService);
				// we only need one agent created per agent type
				if (gatewayAgents.size() > 0 && interfaceAgents.size() > 0) {
					stop();
					addBehaviour(new NutritionBehaviour());
				}
			}
		});
	}

	private class NutritionBehaviour extends SimpleBehaviour {
		private NutritionAgent myAgent;
		private boolean finished = false;
		private String label, calorieCount;
		private int stateCounter = 0;

		public NutritionBehaviour() {
			super(NutritionAgent.this);
			myAgent = NutritionAgent.this;
		}

		@Override
		public void action() {
			ACLMessage msg, reply;
			MessageTemplate template;
			BufferedImage image;
			switch (stateCounter) {
			case 0:
				// listening for label input from Classifier
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId(Constants.LabelSend));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					System.out.println(getLocalName() + " received label from classifier");
					label = msg.getContent();
					stateCounter = 1;
				}
				break;
			case 1:
				// send gateway the label
				if (label != null) {
					sendMsg(label, Constants.LabelSend, ACLMessage.INFORM, myAgent.gatewayAgents);
					System.out.println(getLocalName() + " sent label to gateway");
					stateCounter = 2;
				}
				break;
			case 2:
				// waiting for calorie count from gateway
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId(Constants.CalorieSend));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					System.out.println(getLocalName() + " received calorie count from gateway");
					calorieCount = msg.getContent();
					stateCounter = 3;
				}
				break;
			case 3:
				// sending interface the calories
//	        	if(calorieCount!=null) {
				sendMsg(calorieCount, Constants.CalorieSend, ACLMessage.INFORM, myAgent.interfaceAgents);
				stateCounter = 0;
//	        	}else  stateCounter =0;
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
