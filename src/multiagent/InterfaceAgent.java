package multiagent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import swingui.SwingMain;
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
		new SwingMain();
	}

	private class InterfaceBehaviour extends SimpleBehaviour {

		private InterfaceAgent myAgent;
		private boolean finished = false;
		private int stateCounter = 0;
		private String nutriDataString = "nothing yet";

		public InterfaceBehaviour() {
			super(InterfaceAgent.this);
			myAgent = InterfaceAgent.this;

		}

		@Override
		public void action() {
			ACLMessage msg, reply;
			MessageTemplate template;

			switch (stateCounter) {
			case 0:
				// wait for input from swingUI
				System.out.println(getLocalName() + " case0 - Waiting for input from SwingUI");
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				stateCounter = 1;
				break;
			case 1:
				// send image file path from swingUI to CameraAgent
				sendMsg("/Users/jakfromspace/Downloads/pasta-bowl.jpg", Constants.ImageSend, ACLMessage.INFORM,
						myAgent.cameraAgents);
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
	}

}
