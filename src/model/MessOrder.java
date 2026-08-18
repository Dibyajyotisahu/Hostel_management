package model;

import interfaces.Billable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An extra/ad-hoc mess order placed by a student (beyond the standard mess plan),
 * e.g. guest meals or special snack orders. Demonstrates Composition
 * (owns MessOrderItem objects) and dual-collection usage.
 */
public class MessOrder implements Billable {

    private int orderId;
    private int allocationId;
    private LocalDate orderDate;

    private final List<MessOrderItem> orderItems = new ArrayList<>();      // ordered list for display
    private final Map<Integer, Integer> itemQuantityMap = new HashMap<>(); // itemId -> quantity, O(1) lookup

    public MessOrder(int orderId, int allocationId, LocalDate orderDate) {
        this.orderId = orderId;
        this.allocationId = allocationId;
        this.orderDate = orderDate;
    }

    /** Adds an item to the order, merging quantity if the item is already present. */
    public void addItem(MessItem item, int quantity) {
        Integer existingQty = itemQuantityMap.get(item.getItemId());
        if (existingQty != null) {
            for (MessOrderItem line : orderItems) {
                if (line.getItem().getItemId() == item.getItemId()) {
                    line.setQuantity(line.getQuantity() + quantity);
                    break;
                }
            }
            itemQuantityMap.put(item.getItemId(), existingQty + quantity);
        } else {
            orderItems.add(new MessOrderItem(item, quantity));
            itemQuantityMap.put(item.getItemId(), quantity);
        }
    }

    @Override
    public double calculateCharges() {
        double total = 0;
        for (MessOrderItem line : orderItems) total += line.lineTotal();
        return total;
    }

    @Override
    public String generateBillSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("-- Mess Order #%d (%s) --%n", orderId, orderDate));
        for (MessOrderItem line : orderItems) sb.append(line).append(System.lineSeparator());
        sb.append(String.format("  SUBTOTAL: Rs.%.2f%n", calculateCharges()));
        return sb.toString();
    }

    public int getOrderId() { return orderId; }
    public int getAllocationId() { return allocationId; }
    public LocalDate getOrderDate() { return orderDate; }
    public List<MessOrderItem> getOrderItems() { return orderItems; }
}
