package model;

import interfaces.Billable;

/**
 * Abstract base class representing a hostel room.
 * Concrete subclasses (SingleRoom, DoubleRoom, TripleRoom, DormitoryRoom)
 * provide the occupancy-type-specific monthly fee surcharge and features.
 */
public abstract class Room implements Billable {

    private int roomId;
    private String roomNumber;
    private String block;          // Hostel block/wing, e.g. "A-Block"
    private int floor;
    private int capacity;          // total beds in the room
    private int occupiedBeds;      // currently allotted beds
    private double baseMonthlyFee; // base fee before type surcharge
    private String status;         // AVAILABLE, FULL, MAINTENANCE

    public Room(int roomId, String roomNumber, String block, int floor,
                int capacity, double baseMonthlyFee, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.block = block;
        this.floor = floor;
        this.capacity = capacity;
        this.baseMonthlyFee = baseMonthlyFee;
        this.status = status;
        this.occupiedBeds = 0;
    }

    // ----- Abstract methods every subclass MUST implement -----
    public abstract String getRoomTypeName();
    public abstract String getSpecialFeatures();

    // ----- Billable implementation (shared, uses polymorphic surcharge) -----
    @Override
    public double calculateCharges() {
        return baseMonthlyFee * getSurchargeMultiplier();
    }

    /** Each subclass returns its own multiplier over the base fee. */
    protected abstract double getSurchargeMultiplier();

    @Override
    public String generateBillSummary() {
        return String.format("%s Room %s (%s) - Rs.%.2f/month | Features: %s",
                getRoomTypeName(), roomNumber, block, calculateCharges(), getSpecialFeatures());
    }

    public boolean hasFreeBed() {
        return occupiedBeds < capacity;
    }

    public int getFreeBedCount() {
        return capacity - occupiedBeds;
    }

    // ----- Getters / Setters (Encapsulation) -----
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.isBlank())
            throw new IllegalArgumentException("Room number cannot be empty.");
        this.roomNumber = roomNumber;
    }

    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive.");
        this.capacity = capacity;
    }

    public int getOccupiedBeds() { return occupiedBeds; }
    public void setOccupiedBeds(int occupiedBeds) {
        if (occupiedBeds < 0 || occupiedBeds > capacity)
            throw new IllegalArgumentException("Occupied beds out of valid range.");
        this.occupiedBeds = occupiedBeds;
    }

    public double getBaseMonthlyFee() { return baseMonthlyFee; }
    public void setBaseMonthlyFee(double baseMonthlyFee) {
        if (baseMonthlyFee < 0) throw new IllegalArgumentException("Fee cannot be negative.");
        this.baseMonthlyFee = baseMonthlyFee;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("[%d] Room %s (%s, Floor %d) - %s | %d/%d beds occupied | Rs.%.2f/mo | %s",
                roomId, roomNumber, block, floor, getRoomTypeName(), occupiedBeds, capacity,
                calculateCharges(), status);
    }
}
