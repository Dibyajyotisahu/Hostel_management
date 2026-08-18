package interfaces;

import exception.InvalidPaymentException;
import java.sql.SQLException;

/**
 * Contract for services that process payments against an allocation.
 */
public interface Payable {
    void processPayment(int allocationId, String mode, double amount) throws InvalidPaymentException, SQLException;
    boolean isFullyPaid(int allocationId) throws SQLException;
}
