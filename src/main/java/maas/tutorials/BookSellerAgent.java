package maas.tutorials;

import java.util.Hashtable;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;


@SuppressWarnings("serial")
public class BookSellerAgent extends Agent {
	// The catalogue of paperback books for sale (maps the title of a book to its price)
	private Hashtable _paperBackCatalogue;
	
	// The catalogue of ebooks for sale (maps the title of a book to its price)
	private Hashtable _ebookCatalogue;
	
	// The inventory of paperback books for sale (maps the title of a book to number of available copies)
	private Hashtable _paperBackInventory;
	
	protected void setup() {
	// Printout a welcome message
		System.out.println("Hello! Seller-agent "+getAID().getName()+" is ready.");
		
		createInventory();

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
	
	private void createInventory() {
		Book_Titles titles = Book_Titles.getInstance();
		int agentNumber = getAgentNumber();
		
		_paperBackCatalogue = new Hashtable();
		_ebookCatalogue = new Hashtable();
		_paperBackInventory = new Hashtable();
		
		for (int i = 0; i < 4; i++) {
			int titleIdx = i + agentNumber;
			
			// Use the titles as a circular buffer
			if (titleIdx >= titles.titles.size()) {
				titleIdx = titleIdx - titles.titles.size();
			}
			
			_paperBackCatalogue.put(titles.titles.get(titleIdx), (50 * (i+1)));
			_ebookCatalogue.put(titles.titles.get(titleIdx), (25 * (i+1)));
			_paperBackInventory.put(titles.titles.get(titleIdx), 5);
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
