package multiagent;

import jade.core.Agent;

import java.util.concurrent.TimeUnit;

import jade.core.AID;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class MakerAgent extends Agent {

	protected void setup() {
		System.out.println("Hello World! Jade has been Booted...");
		Object[] args = getArguments();
		int number = Integer.parseInt((String) args[0]);
		
		try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }

		System.out.println("\nMakerAgent: Created with number " + number);

		createAgent("InterfaceAgent", "multiagent.InterfaceAgent");
		//		  createAgent("Camera-1", "tuto.first.CameraAgent");
		//	      createAgent("Classifier-1", "tuto.first.ClassifierAgent");
		//	      createAgent("Nutrition-1", "tuto.first.NutritionAgent");
	}

	private void createAgent(String name, String className) {
		AID agentID = new AID(name, AID.ISLOCALNAME);
		AgentContainer controller = getContainerController();
		try {
			AgentController agent = controller.createNewAgent(name, className, null);
			agent.start();
			System.out.println("\nInitialized: " + agentID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
