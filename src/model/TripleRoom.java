package model;

/** Three-sharing room - budget-friendly shared living. */
public class TripleRoom extends Room {

    private boolean hasStudyLounge;

    public TripleRoom(int roomId, String roomNumber, String block, int floor,
                       double baseMonthlyFee, String status, boolean hasStudyLounge) {
        super(roomId, roomNumber, block, floor, 3, baseMonthlyFee, status);
        this.hasStudyLounge = hasStudyLounge;
    }

    @Override
    public String getRoomTypeName() { return "TRIPLE"; }

    @Override
    public String getSpecialFeatures() {
        return "3 Beds | Shared Wardrobe | WiFi" + (hasStudyLounge ? " | Shared Study Lounge" : "");
    }

    @Override
    protected double getSurchargeMultiplier() { return 1.15; }

    public boolean isHasStudyLounge() { return hasStudyLounge; }
    public void setHasStudyLounge(boolean hasStudyLounge) { this.hasStudyLounge = hasStudyLounge; }
}
