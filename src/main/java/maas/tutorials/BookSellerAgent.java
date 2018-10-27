package maas.tutorials;

import java.util.Hashtable;
import java.util.Set;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.Agent;
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
public class BookSellerAgent extends Agent {
    // The catalogue of paperback books for sale (maps the title of a book to its price)
    private Hashtable _paperBackCatalogue;
    
    // The catalogue of ebooks for sale (maps the title of a book to its price)
    private Hashtable _ebookCatalogue;
    
    // The inventory of paperback books for sale (maps the title of a book to number of available copies)
    private Hashtable _paperBackInventory;
    
    public int getPrice(String content) {
        String[] contentParts = content.split(":");
        String title = contentParts[1];
        boolean is_ebook = contentParts[0] == "Ebook";

        int price = -1;
        if (is_ebook) {
            price = (Integer) _ebookCatalogue.get(title);
        }
        else if (_paperBackInventory.containsKey(title)){
            int num_of_copies = (Integer) _paperBackInventory.get(title);
            if (num_of_copies > 0) {
                price = (Integer) _paperBackCatalogue.get(title);
            }
        }
        return price;
    }

    protected void setup() {
    // Printout a welcome message
        System.out.println("Hello! Seller-agent "+getAID().getLocalName()+" is ready.");
        
        createInventory();
//        displayInventory();

        registerInYellowPages();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
//        addBehaviour(new shutdown());

        // Add the behaviour serving queries from buyer agents
        addBehaviour(new QuotationRequestsServer());

        // Add the behaviour serving purchase orders from buyer agents
        addBehaviour(new PurchaseOrdersServer());

    }

    protected void registerInYellowPages() {
        // Register the book-selling service in the yellow pages

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-seller");
        sd.setName("Book-trading");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    
    protected void deregisterFromYellowPages() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    protected void takeDown() {
        deregisterFromYellowPages();
        
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
    
    public void displayInventory() {
        Set<String> titles = _paperBackInventory.keySet();
        System.out.println("\n==========================================");
        System.out.println("Inventory of seller agent: " + getAID().getLocalName());
        System.out.println("==========================================\n");
        System.out.println("Title | Paperback Count | Paperback cost | Ebook cost |");
        System.out.println("--------------------------------------------");

        for(String t : titles) {
            StringBuilder sb = new StringBuilder();
            sb.append(t);
            sb.append(" | ");
            sb.append(_paperBackInventory.get(t));
            sb.append(" | ");
            sb.append(_paperBackCatalogue.get(t));
            sb.append(" | ");
            sb.append(_ebookCatalogue.get(t));
            sb.append(" | ");
            System.out.println(sb.toString());
        }

        System.out.println("==========================================\n");
    }

    private boolean removeFromInventory(String title) {
        int count = (Integer)_paperBackInventory.get(title);
        boolean success = false;
        if (count > 0) {
            _paperBackInventory.put(title, count - 1);
            success = true;
        }
        return success;
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

    private class QuotationRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it
                ACLMessage reply = msg.createReply();

                String content = msg.getContent();
                System.out.println(myAgent.getAID().getLocalName() + " Received quotation request for title " + content);

                int price = ((BookSellerAgent)myAgent).getPrice(content);

                if (price >= 0) {
                    // The requested book is available for sale. Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(Integer.toString(price));
                    System.out.println(myAgent.getAID().getLocalName() + " Quoted Price: "+ Integer.toString(price));
                }
                else {
                    // The requested book is NOT available for sale.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                    System.out.println(myAgent.getAID().getLocalName() + " Refused request");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer
    
    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // ACCEPT_PROPOSAL Message received. Process it
                ACLMessage reply = msg.createReply();

                String content = msg.getContent();
                String[] contentParts = content.split(":");
                String title = contentParts[1];

                int price = ((BookSellerAgent)myAgent).getPrice(content);

                if (price >= 0) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(myAgent.getAID().getLocalName() + " sold " + title + " to  "+msg.getSender().getLocalName());
                }
                else {
                    // The requested book has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer

}
