package exception;

/**
 * Checked exception thrown for invalid payment mode or non-positive amount.
 */
public class InvalidPaymentException extends Exception {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
