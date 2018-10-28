package maas.tutorials;

public class Transaction {
    public String _type;
    public String _title;
    public String _agent;
    public int _price;

    public Transaction(String type, String title, int price, String agent) {
        _type = type;
        _title = title;
        _price = price;
        _agent = agent;
    }

    public void print() {
        System.out.println(getAsString());
    }

    public String getAsString() {
        return "Type: " + _type + ", Title: " + _title + ", Price: " + _price + ", Interacting agent: " + _agent;
    }
}