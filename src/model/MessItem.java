package model;

/** A single item on the hostel mess menu. */
public class MessItem {

    private int itemId;
    private String name;
    private String category; // BREAKFAST, LUNCH, DINNER, SNACKS, BEVERAGES, DESSERTS
    private double price;

    public MessItem(int itemId, String name, String category, double price) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        setPrice(price);
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative.");
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("[%d] %-22s (%-10s) Rs.%.2f", itemId, name, category, price);
    }
}
