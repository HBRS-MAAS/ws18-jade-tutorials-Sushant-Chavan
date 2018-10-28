package maas.tutorials;

import java.util.List;
import java.util.Vector;

// Singleton class to access the book titles
public class Book_Titles {
    // static variable single_instance of type Singleton
    private static Book_Titles _single_instance = null;

    // variable of type String
    public List<String> _titles;

    // private constructor restricted to this class itself
    private Book_Titles() {
        _titles = new Vector<>();

        _titles.add("An Introduction to Multiagent Systems");
        _titles.add("Understanding Agent Systems");
        _titles.add("Neural Networks");
        _titles.add("Automated Planning- Theory and Practice");
        _titles.add("A Modern Approach to AI");
        _titles.add("Introduction to Robotics");
    }

    // static method to create instance of Singleton class
    public static Book_Titles getInstance() {
        if (_single_instance == null)
            _single_instance = new Book_Titles();

        return _single_instance;
    }
}
