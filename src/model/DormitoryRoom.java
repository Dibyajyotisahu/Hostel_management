package model;

/** Large dormitory-style room - cheapest, highest capacity. */
public class DormitoryRoom extends Room {

    private int lockerCount;

    public DormitoryRoom(int roomId, String roomNumber, String block, int floor,
                          double baseMonthlyFee, String status, int lockerCount) {
        super(roomId, roomNumber, block, floor, 6, baseMonthlyFee, status);
        this.lockerCount = lockerCount;
    }

    @Override
    public String getRoomTypeName() { return "DORMITORY"; }

    @Override
    public String getSpecialFeatures() {
        return "6 Bunk Beds | " + lockerCount + " Personal Lockers | Common WiFi";
    }

    @Override
    protected double getSurchargeMultiplier() { return 1.00; }

    public int getLockerCount() { return lockerCount; }
    public void setLockerCount(int lockerCount) { this.lockerCount = lockerCount; }
}
