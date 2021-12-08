package multiagent;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Constants;

public class CameraAgent extends ServiceAgent {

	public Set<AID> classifierAgents = new HashSet<>();// set that contains a list of agents

	@Override
	protected void setup() {
		register(Constants.CameraService);
		addBehaviour(new TickerBehaviour(this, 2000) {
			protected void onTick() {
				classifierAgents = searchForService(Constants.ClassifierService);
				if (classifierAgents.size() > 0) {
					stop();
					addBehaviour(new CameraBehaviour());
				}
			}
		});
	}

	private class CameraBehaviour extends SimpleBehaviour {
		private CameraAgent myAgent;
		private boolean finished = false;
		private BufferedImage image = null;
		private String imagePath = null;
		private int stateCounter = 0;
		private final String formatName = "jpg";

		public CameraBehaviour() {
			super(CameraAgent.this);
			myAgent = CameraAgent.this;
		}

		@Override
		public void action() {
			ACLMessage msg, reply;
			MessageTemplate template;

			switch (stateCounter) {
			case 0:
				// listening for image input from Interface
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId(Constants.ImageSend));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					System.out.println(getLocalName() + " received image path from interface");
					imagePath = msg.getContent();
					stateCounter = 1;
				}
				break;
			case 1:
				// send ClassifierAgent the base64 converted image
				image = readImage(imagePath);
				String b64img = imgToBase64String(image, formatName);
				sendMsg(b64img, Constants.Base64Send, ACLMessage.INFORM, myAgent.classifierAgents);
				System.out.println(getLocalName() + " sent base64 to Classifier");
				stateCounter = 0;
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

		// image to Base64
		public static String imgToBase64String(final BufferedImage img, final String formatName) {
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				ImageIO.write(img, formatName, os);
				return Base64.getEncoder().encodeToString(os.toByteArray());
			} catch (final IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}

		// read image from path
		public BufferedImage readImage(String path) {
			BufferedImage img = null;
			try {
				img = ImageIO.read(new File(path));
				return img;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return img;
		}

		@Override
		public boolean done() {
			return finished;
		}

	}
}