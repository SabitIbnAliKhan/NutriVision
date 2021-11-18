package multiagent;

import jade.core.Agent;

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
				if (cameraAgents.size() >= 1 && nutritionAgents.size() >= 1) {
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
				
				break;
			case 1:
				sendMsg("path/to/image/file.png", Constants.ImageSend, ACLMessage.REQUEST, myAgent.cameraAgents);
				// send image file path from swingUI to CameraAgent
				break;
			case 2:
				// wait for processing and reply from NutritionAgent
				break;
			case 3:
				// send back calorie data from NutritionAgent to swingUI
				stateCounter = 0;
				break;
			default:
				System.out.println("Interface: Invalid state. Resetting to 0");
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
