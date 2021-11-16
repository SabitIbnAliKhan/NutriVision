package multiagent;

import jade.core.Agent;
import jade.core.AID;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;


public class InstantiatorAgent extends Agent {

	  protected void setup() {
		  
	        createAgent("Camera-1", "tuto.first.CameraAgent");
	        createAgent("Classifier-1", "tuto.first.ClassifierAgent");
	        createAgent("Nutrition-1", "tuto.first.NutritionAgent");
	  }

	  private void createAgent(String name, String className) {
	      	AID agentID = new AID( name, AID.ISLOCALNAME );
	      	AgentContainer controller = getContainerController();
	      	try {
	      		AgentController agent = controller.createNewAgent( name, className, null );
	      		agent.start();
	      		System.out.println("Initialized: " + agentID);
	      	}
	      	catch (Exception e){ e.printStackTrace(); }
	  }
}
