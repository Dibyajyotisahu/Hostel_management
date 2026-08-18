package exception;

/**
 * Checked exception thrown for invalid allocation data:
 * missing student/room, invalid dates, or invalid bed number.
 */
public class InvalidAllocationException extends Exception {
    public InvalidAllocationException(String message) {
        super(message);
    }
}
