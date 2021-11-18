package multiagent;

import jade.core.Agent;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CameraAgent extends ServiceAgent{
	
	
	public Set<AID> classifierAgents = new HashSet<>();
	
	@Override
	protected void setup() {
	      System.out.printf("Camera %s is initialized%n", getLocalName());
	      register("image_service");
	      addBehaviour(new TickerBehaviour (this, 2000) {
	          protected void onTick() {
	        	  classifierAgents = searchForService("classifier_service");
	            if (classifierAgents.size() >= 1) {
	                stop();
	      	      addBehaviour(new CameraBehaviour());
	            }
	          }
	        });
	}
	
	private class CameraBehaviour extends SimpleBehaviour{
		private CameraAgent myAgent;
	    private boolean finished = false;
	    private int stateCounter = 0; //changes to 0 when case 0 is taken from interfaceAgent
	    
		public CameraBehaviour(){
			super(CameraAgent.this);
			myAgent = CameraAgent.this;
		}

		@Override
		public void action() {
	        ACLMessage msg, reply;
	        MessageTemplate template;
	        BufferedImage image;
	        switch(stateCounter) {
	        case 0: 
	        	//listening for image input from Interface
	        	stateCounter = 1;
	        	break;
	        case 1:
	        	//send ClassifierAgent the base64 converted image
	            sendMsg("Here is the Base-64 image", "image_transfer", ACLMessage.REQUEST, myAgent.classifierAgents);
	            stateCounter = 20;
	            break;
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
        
        //add method to convert img to bin-64
        
		@Override
		public boolean done() {
			return finished;
		}
		
	}
}