package maas.tutorials;

import java.util.List;
import java.util.Vector;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.MessageTemplate;


@SuppressWarnings("serial")
public class BookBuyerAgent extends Agent {
	protected List<String> _titlesToPurchase;
	protected List<String> _purchasedTitles;

    // The list of available seller agents
    private AID[] sellerAgents;
    
    protected void setup() {
    // Printout a welcome message
        System.out.println("Hello! Buyer-agent "+getAID().getName()+" is ready.");
        
        _purchasedTitles = new Vector<>();
        
        determineTitlesToBuy();
//        displayTitlesToBuy();
        
        discoverSellers();

        try {
            Thread.sleep(3000);
 		} catch (InterruptedException e) {
 			//e.printStackTrace();
 		}
		addBehaviour(new shutdown());

	}
	
	protected void takeDown() {
		System.out.println(getAID().getLocalName() + ": Terminating.");
	}
	
	protected int getAgentNumber( ) {
		String[] parts = getAID().getLocalName().split("-");
		return Integer.parseInt(parts[1]);		
	}
	
	protected void determineTitlesToBuy() {
		_titlesToPurchase = new Vector<>();
		int agentNum = getAgentNumber();
		
		Book_Titles titles = Book_Titles.getInstance();
		
		_titlesToPurchase.add(titles.titles.get(agentNum % titles.titles.size()));
		_titlesToPurchase.add(titles.titles.get((agentNum + 1) % titles.titles.size()));
		_titlesToPurchase.add(titles.titles.get((agentNum + 2) % titles.titles.size()));
	}
	
	protected void displayTitlesToBuy() {		
		System.out.println("\n==========================================");
		System.out.println("Items to buy by buyer agent: " + getAID().getLocalName());
		System.out.println("==========================================\n");
		System.out.println(_titlesToPurchase);
		System.out.println("==========================================\n");
	}
	
	protected void purchasedTitles() {
		if (!_purchasedTitles.isEmpty()) {
			System.out.println("\n==========================================");
			System.out.println("Items purchased by buyer agent: " + getAID().getLocalName());
			System.out.println("==========================================\n");
			System.out.println(_purchasedTitles);
			System.out.println("==========================================\n");
		}
    }
    
    protected void discoverSellers() {
     // Update the list of seller agents
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-seller");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            sellerAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                sellerAgents[i] = result[i].getName();
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    // Taken from http://www.rickyvanrijn.nl/2017/08/29/how-to-shutdown-jade-agent-platform-programmatically/
    private class shutdown extends OneShotBehaviour{
		public void action() {
			ACLMessage shutdownMessage = new ACLMessage(ACLMessage.REQUEST);
			Codec codec = new SLCodec();
			myAgent.getContentManager().registerLanguage(codec);
			myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
			shutdownMessage.addReceiver(myAgent.getAMS());
			shutdownMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
			shutdownMessage.setOntology(JADEManagementOntology.getInstance().getName());
			try {
			    myAgent.getContentManager().fillContent(shutdownMessage,new Action(myAgent.getAID(), new ShutdownPlatform()));
			    myAgent.send(shutdownMessage);
			}
			catch (Exception e) {
			    //LOGGER.error(e);
			}

		}
	}
}
