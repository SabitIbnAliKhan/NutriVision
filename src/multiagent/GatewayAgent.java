package multiagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Constants;


public class GatewayAgent extends ServiceAgent {

	public Set<AID> classifierAgents = new HashSet<>();// set that contains a list of agents
	public Set<AID> nutritionAgents = new HashSet<>();
	
	@Override
	protected void setup() {
		register(Constants.GatewayService);
		addBehaviour(new TickerBehaviour(this, 2000) {
			protected void onTick() {
				classifierAgents = searchForService(Constants.ClassifierService);
				nutritionAgents = searchForService(Constants.NutritionService);
				if (classifierAgents.size() > 0 && nutritionAgents.size() > 0) {
					stop();
					addBehaviour(new GatewayBehaviour());
				}
			}
		});
	}

	private class GatewayBehaviour extends SimpleBehaviour {
		private GatewayAgent myAgent;
		private boolean finished = false;
		private int stateCounter = 0;
		private String base64img; 
		private String label;
		private String calories;

		public GatewayBehaviour() {
			super(GatewayAgent.this);
			myAgent = GatewayAgent.this;
		}

		@Override
		public void action() {
			ACLMessage msg, reply;
			MessageTemplate template;
			
			switch (stateCounter) {
			case 0:
				//listening for base64img from classifier
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId(Constants.Base64Send));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					base64img = msg.getContent();
					System.out.println(getLocalName() + " received the base64 image from Classifier:\n " + base64img);
					stateCounter = 1;
				}
				break;
			case 1:
				//API call for label then send to classifier
				//Smartlens API was needed to turn base64 to label but the site is terminated...
				//We Assume the label in the JSON response to be Pasta for testing purposes
				sendMsg("Pasta", Constants.LabelSend, ACLMessage.INFORM, myAgent.classifierAgents);
				System.out.println(getLocalName() + " sent the label to Classifier");
				stateCounter = 2;
				break;
			case 2:
				//listening for label from nutrition
				template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId(Constants.LabelSend));
				msg = myAgent.blockingReceive(template);
				if (msg != null) {
					System.out.println(getLocalName() + " received the label from Nutrition");
					label = msg.getContent();
					stateCounter = 3;
				}
				break;
			case 3:
				//API call for calories
				//String response = sendGetRequest("https://www.nutritionix.com/natural-demo?q=" + label);
				sendPOSTRequest();
				//System.out.println(response);
				//parse calories from JSON response
				stateCounter = 4;
				break;
			case 4:
				//send nutrition the calories
				sendMsg(calories, Constants.CalorieSend, ACLMessage.INFORM, myAgent.nutritionAgents);
				System.out.println(getLocalName() + " sent the calories to Nutrition");
				finished = true;
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
		
		 private static void sendPOSTRequest(){
		        try {
		            String post_data="key1=value1&key2=value2";

		            URL url = new URL("https://postman-echo.com/post");
		            HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
		            httpURLConnection.setRequestMethod("POST");
		            //adding header
		            httpURLConnection.setRequestProperty("Auth","Token");
		            httpURLConnection.setRequestProperty("Data1","Value1");
		            httpURLConnection.setDoOutput(true);

		            //Adding Post Data
		            OutputStream outputStream=httpURLConnection.getOutputStream();
		            outputStream.write(post_data.getBytes());
		            outputStream.flush();
		            outputStream.close();



		            System.out.println("Response Code "+httpURLConnection.getResponseCode());

		            String line="";
		            InputStreamReader inputStreamReader=new InputStreamReader(httpURLConnection.getInputStream());
		            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
		            StringBuilder response=new StringBuilder();
		            while ((line=bufferedReader.readLine())!=null){
		                response.append(line);
		            }
		            bufferedReader.close();
		            System.out.println("Response : "+response.toString());



		        }
		        catch (Exception e){
		            e.printStackTrace();
		            System.out.println("Error in Making POST Request");
		        }
		    }

		 private static String sendGetRequest(String uri) {
			StringBuffer responseContent = new StringBuffer();
			BufferedReader reader;
			HttpURLConnection connection;
			String line;
			try {
				URL url = new URL(uri);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(5000);
				int status = connection.getResponseCode();
				System.out.println("Status: " + status);
				
				if (status > 299) {
					reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
					while((line = reader.readLine()) != null) {
						responseContent.append(line);
					}
					reader.close();
				} else {
					reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					while((line = reader.readLine()) != null) {
						responseContent.append(line);
					}
					reader.close();
					return responseContent.toString();
				}				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		public boolean done() {
			return finished;
		}

	}

}
