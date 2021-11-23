package multiagent;

import jade.core.Agent;
import java.util.Set;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.HashSet;
import jade.core.AID;

public class ServiceAgent extends Agent {
	
	protected Set<AID> searchForService(String serviceName){
		Set<AID> foundAgts = new HashSet<>();
		DFAgentDescription dfad = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(serviceName.toLowerCase());
		dfad.addServices(sd);
		
		SearchConstraints sc = new SearchConstraints();
		sc.setMaxResults(Long.valueOf(-1));
		try {
			DFAgentDescription[] results = DFService.search(this, dfad, sc);
			for(DFAgentDescription result : results) {
				foundAgts.add(result.getName());
			}
			return foundAgts;
		}
		catch (FIPAException ex) { ex.printStackTrace(); return null; }
	}
	
	protected void takeDown() {
		System.out.println(this.getLocalName() + " is now terminated");
		try { DFService.deregister(this); }
		catch (Exception ex) {}
	}
	
	protected void register(String serviceName) {
		DFAgentDescription dfad = new DFAgentDescription();
		dfad.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName());
		sd.setType(serviceName.toLowerCase());
		dfad.addServices(sd);
		try {
			DFService.register(this, dfad);
		}
		catch (FIPAException ex) { ex.printStackTrace();}
	}
}
