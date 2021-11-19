package multiagent;

import jade.core.Agent;

import java.util.concurrent.TimeUnit;

import jade.core.AID;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class MakerAgent extends Agent {

	protected void setup() {
		System.out.println("Hello World! Jade has been Booted...");
		try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }

		//	System.out.println("\nMakerAgent: Created with number " + number);

		createAgent("Interface-1", "multiagent.InterfaceAgent");
		
		createAgent("Camera-1", "multiagent.CameraAgent");
	    createAgent("Classifier-1", "multiagent.ClassifierAgent");
	    createAgent("Nutrition-1", "multiagent.NutritionAgent");
	    
	    createAgent("Gateway-1", "multiagent.GatewayAgent");
	}

	private void createAgent(String name, String className) {
		AID agentID = new AID(name, AID.ISLOCALNAME);
		AgentContainer controller = getContainerController();
		System.out.println("\nMakerAgent: Creating agents");
		try {
			AgentController agent = controller.createNewAgent(name, className, null);
			agent.start();
			System.out.println("Initialized: " + agentID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
