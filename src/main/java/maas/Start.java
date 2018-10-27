package maas;

import java.util.List;
import java.util.Vector;
import maas.tutorials.BookBuyerAgent;

public class Start {
    public static void main(String[] args) {
    	int num_of_buyers = 20;
    	int num_of_sellers = 3;
    	
    	List<String> agents = new Vector<>();

        for (int i = 0; i < num_of_sellers; i++) {
            StringBuilder name_builder = new StringBuilder();
            name_builder.append("Seller-");
            name_builder.append(Integer.toString(i));
            name_builder.append(":maas.tutorials.BookSellerAgent");
            
            agents.add(name_builder.toString());
        }

    	for (int i = 0; i < num_of_buyers; i++) {
    		StringBuilder name_builder = new StringBuilder();
    		name_builder.append("Buyer-");
    		name_builder.append(Integer.toString(i));
    		name_builder.append(":maas.tutorials.BookBuyerAgent");
    		
    		agents.add(name_builder.toString());
    	}

    	List<String> cmd = new Vector<>();
    	cmd.add("-agents");
    	StringBuilder sb = new StringBuilder();
    	for (String a : agents) {
    		sb.append(a);
    		sb.append(";");
    	}
    	cmd.add(sb.toString());
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }
}
