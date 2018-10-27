package maas.tutorials;

import java.util.List;
import java.util.Vector;
import java.lang.*;

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

    protected void setup() {
    // Printout a welcome message
        System.out.println("Hello! Buyer-agent "+getAID().getLocalName()+" is ready.");
        
        _purchasedTitles = new Vector<>();
        
        determineTitlesToBuy();
//        displayTitlesToBuy();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        
        addBehaviour(new BookRequester());
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

       private class BookRequester extends Behaviour {
            private AID bestSeller; // The agent who provides the best offer 
            private int bestPrice;  // The best offered price
            private int repliesCnt = 0; // The counter of replies from seller agents
            private MessageTemplate mt; // The template to receive replies
            private int step = 0;
            private String bookType;
            private String conversationID;
            private AID[] sellerAgents;
            private String targetBookTitle;

            protected void discoverSellers() {
                // Update the list of seller agents
                   DFAgentDescription template = new DFAgentDescription();
                   ServiceDescription sd = new ServiceDescription();
                   sd.setType("book-seller");
                   template.addServices(sd);
                   try {
                       DFAgentDescription[] result = DFService.search(myAgent, template);
                       sellerAgents = new AID[result.length];
                       for (int i = 0; i < result.length; ++i) {
                           sellerAgents[i] = result[i].getName();
                       }
                   }
                   catch (FIPAException fe) {
                       fe.printStackTrace();
                   }

//                   for (AID a : sellerAgents) {
//                       System.out.println("Found seller: " + a.getLocalName());
//                   }
               }

            public void action() {
                if (_purchasedTitles.size() >= _titlesToPurchase.size()) {
                    // Purchased all books. Delete te agent.
                    myAgent.addBehaviour(new shutdown());
                }

                switch (step) {
                case 0:
                    // Always order the second book as an Ebook
                    bookType = (_purchasedTitles.size() != 1) ? "Ebook:" : "Paperback:";
                    targetBookTitle = bookType + _titlesToPurchase.get(_purchasedTitles.size());
                    conversationID = "book-trade - " + Integer.toString(_purchasedTitles.size());
                    discoverSellers();

                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < sellerAgents.length; ++i) {
                        cfp.addReceiver(sellerAgents[i]);
                    }
                    cfp.setContent(targetBookTitle);
                    cfp.setConversationId(conversationID);
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer 
                            int price = Integer.parseInt(reply.getContent());
                            if (bestSeller == null || price < bestPrice) {
                                // This is the best offer at present
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= sellerAgents.length) {
                            // We received all replies
                            step = 2; 
                        }
                    }
                    else {
                        block();
                    }
                    break;
                case 2:
                    // Send the purchase order to the seller that provided the best offer

                    if (targetBookTitle == null) {
                        bookType = (_purchasedTitles.size() == 1) ? "Ebook:" : "Paperback:";
                        targetBookTitle = bookType + _titlesToPurchase.get(_purchasedTitles.size());
                    }
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(targetBookTitle);
                    order.setConversationId(conversationID);
                    order.setReplyWith("order"+System.currentTimeMillis());
                    myAgent.send(order);
                    // Prepare the template to get the purchase order reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    // Receive the purchase order reply
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Purchase order reply received
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // Purchase successful. We can terminate
                            System.out.println(myAgent.getAID().getLocalName() + " purchased " + _titlesToPurchase.get(_purchasedTitles.size()) +" for "+ bestPrice + " from "+reply.getSender().getLocalName() );
                            _purchasedTitles.add(_titlesToPurchase.get(_purchasedTitles.size()));
                        }
                        else {
                            System.out.println(myAgent.getAID().getLocalName() + " Attempt failed: requested book already sold.");
                        }

                        // Restart the purchase for new book.
                        step = 0;
                        bestSeller = null; 
                        bestPrice = -1;
                        repliesCnt = 0;
                        mt = null;
                        bookType = null;
                        conversationID = null;
                    }
                    else {
                        block();
                    }
                    break;
                }
            }

            public boolean done() {
                if (step == 2 && bestSeller == null) {
                    bookType = (_purchasedTitles.size() == 1) ? "Ebook:" : "Paperback:";
                    String targetBookTitle = bookType + _titlesToPurchase.get(_purchasedTitles.size());
                    System.out.println("Attempt failed: "+targetBookTitle+" not available for sale");
                }
                return ((step == 2 && bestSeller == null) || step == 4);
            }
        }  // End of inner class RequestPerformer
}
