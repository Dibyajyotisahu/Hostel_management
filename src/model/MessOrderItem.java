package model;

/**
 * A line item inside a MessOrder: a MessItem plus the quantity ordered.
 * Owned exclusively by its parent MessOrder (Composition).
 */
public class MessOrderItem {

    private MessItem item;
    private int quantity;

    public MessOrderItem(MessItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public double lineTotal() {
        return item.getPrice() * quantity;
    }

    public MessItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("  %-22s x%-3d Rs.%.2f", item.getName(), quantity, lineTotal());
    }
}
