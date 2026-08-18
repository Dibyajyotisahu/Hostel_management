package interfaces;

/**
 * Contract for any entity that can be billed (Room, MessOrder).
 */
public interface Billable {
    double calculateCharges();
    String generateBillSummary();
}
