package model;

/** Single-occupancy room - most private, highest monthly fee. */
public class SingleRoom extends Room {

    private boolean hasAttachedBathroom;

    public SingleRoom(int roomId, String roomNumber, String block, int floor,
                       double baseMonthlyFee, String status, boolean hasAttachedBathroom) {
        super(roomId, roomNumber, block, floor, 1, baseMonthlyFee, status);
        this.hasAttachedBathroom = hasAttachedBathroom;
    }

    @Override
    public String getRoomTypeName() { return "SINGLE"; }

    @Override
    public String getSpecialFeatures() {
        return "Study Table | Wardrobe | WiFi" + (hasAttachedBathroom ? " | Attached Bathroom" : "");
    }

    @Override
    protected double getSurchargeMultiplier() { return 1.60; }

    public boolean isHasAttachedBathroom() { return hasAttachedBathroom; }
    public void setHasAttachedBathroom(boolean hasAttachedBathroom) { this.hasAttachedBathroom = hasAttachedBathroom; }
}
