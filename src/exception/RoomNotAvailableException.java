package exception;

/**
 * Unchecked exception thrown when a room/bed is already occupied or has
 * no free capacity for allocation.
 */
public class RoomNotAvailableException extends RuntimeException {
    public RoomNotAvailableException(String message) {
        super(message);
    }
}
