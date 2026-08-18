package model;

/** Two-sharing room - moderate privacy and fee. */
public class DoubleRoom extends Room {

    private boolean hasBalcony;

    public DoubleRoom(int roomId, String roomNumber, String block, int floor,
                       double baseMonthlyFee, String status, boolean hasBalcony) {
        super(roomId, roomNumber, block, floor, 2, baseMonthlyFee, status);
        this.hasBalcony = hasBalcony;
    }

    @Override
    public String getRoomTypeName() { return "DOUBLE"; }

    @Override
    public String getSpecialFeatures() {
        return "2 Study Tables | 2 Wardrobes | WiFi" + (hasBalcony ? " | Balcony" : "");
    }

    @Override
    protected double getSurchargeMultiplier() { return 1.30; }

    public boolean isHasBalcony() { return hasBalcony; }
    public void setHasBalcony(boolean hasBalcony) { this.hasBalcony = hasBalcony; }
}
