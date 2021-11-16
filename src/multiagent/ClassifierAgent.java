package multiagent;

import java.awt.image.BufferedImage;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ClassifierAgent extends ServiceAgent{

	@Override
	protected void setup() {
	      System.out.printf("Classifier %s is initialized%n", getLocalName());
	      register("classifier_service");
	      addBehaviour(new ClassifierBehaviour());
	}
	
	private class ClassifierBehaviour extends SimpleBehaviour{
		
	    private boolean finished = false;
	    private int stateCounter = 0; //changes to 0 when case 0 is taken from interfaceAgent
	    
		public ClassifierBehaviour(){
			super(ClassifierAgent.this);
			myAgent = ClassifierAgent.this;
		}

		@Override
		public void action() {
	        ACLMessage msg, reply;
	        MessageTemplate template;

	        switch(stateCounter) {
	        case 0: 
	        	//listening for image base64 from CameraAgent
	            //listening for game REQUEST
	            template = MessageTemplate.and(
	                      MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
	                      MessageTemplate.MatchConversationId("image_transfer"));
	            msg = myAgent.blockingReceive(template);
	            if(msg != null) {
	              System.out.println(getLocalName() + " received an image");
	              System.out.println("This image is: " + msg.getContent());
	              stateCounter = 1;
	            }
	          break;
	        case 1:
	        	//send NutritionAgent & GatewayAgent the base64 to get label later
	            //  sendMsg("Here is the Base-64 image", "image_transfer_2", ACLMessage.INFORM, gateWayAgent);
		        //  sendMsg("Here is the Label", "label_transfer", ACLMessage.INFORM, nutritionAgent);

			}
		}
		
        private void sendMsg(String content, String conversationId, int type, Set<AID> receivers) {
            ACLMessage msg = new ACLMessage(type);
            msg.setContent(content);
            msg.setConversationId(conversationId);
            //add receivers
            for (AID agent: receivers) {
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
