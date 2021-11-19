package multiagent;

import java.util.HashSet;
import java.util.Set;

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
				System.out.println("Interface: case0 - Waiting for input from SwingUI");
				break;
			case 1:
				// send image file path from swingUI to CameraAgent
				sendMsg("path/to/image/file.png", Constants.ImageSend, ACLMessage.INFORM, myAgent.cameraAgents);
				System.out.println("Interface: case1 - Image path sent to CameraAgent");
				stateCounter = 2;
				break;
			case 2:
				// wait for processing and reply from NutritionAgent
				System.out.println("Interface: case2 - waiting for reply from NutritionAgent");
				stateCounter = 3;
				break;
			case 3:
				// send back calorie data from NutritionAgent to swingUI
				System.out.println("Interface: case3 - Calorie data sent to swingUI");
				stateCounter = 0;
				break;
			default:
				System.out.println("Interface: caseError - Invalid state. Resetting to 0");
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
