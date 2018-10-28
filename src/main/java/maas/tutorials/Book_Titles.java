package maas.tutorials;

import java.util.List;
import java.util.Vector;

// Singleton class to access the book titles
public class Book_Titles {
    // static variable single_instance of type Singleton
    private static Book_Titles single_instance = null;

    // variable of type String
    public List<String> titles;

    // private constructor restricted to this class itself
    private Book_Titles() {
        titles = new Vector<>();

        titles.add("An Introduction to Multiagent Systems");
        titles.add("Understanding Agent Systems");
        titles.add("Neural Networks");
        titles.add("Automated Planning- Theory and Practice");
        titles.add("A Modern Approach to AI");
        titles.add("Introduction to Robotics");
    }

    // static method to create instance of Singleton class
    public static Book_Titles getInstance() {
        if (single_instance == null)
            single_instance = new Book_Titles();

        return single_instance;
    }
}
