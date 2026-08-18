package dao;

import interfaces.Searchable;
import model.*;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Access Object for Room (and its subclasses).
 * Uses the Factory pattern (buildRoom) to reconstruct the correct
 * polymorphic subclass instance from a database row.
 */
public class RoomDAO implements Searchable<Room> {

    public int insert(Room r) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, room_type, block, floor, capacity, " +
                "base_monthly_fee, occupied_beds, status, feature_flag) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getRoomNumber());
            ps.setString(2, r.getRoomTypeName());
            ps.setString(3, r.getBlock());
            ps.setInt(4, r.getFloor());
            ps.setInt(5, r.getCapacity());
            ps.setDouble(6, r.getBaseMonthlyFee());
            ps.setInt(7, r.getOccupiedBeds());
            ps.setString(8, r.getStatus());
            ps.setInt(9, extractFeatureFlag(r));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateStatusAndOccupancy(int roomId, String status, int occupiedBeds) throws SQLException {
        String sql = "UPDATE rooms SET status=?, occupied_beds=? WHERE room_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, occupiedBeds);
            ps.setInt(3, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Room findById(int id) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return buildRoom(rs);
            }
        }
        return null;
    }

    public List<Room> findAll() throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(buildRoom(rs));
        }
        return list;
    }

    public List<Room> findByType(String roomType) throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE room_type=? ORDER BY room_number";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, roomType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(buildRoom(rs));
            }
        }
        return list;
    }

    /** Rooms whose computed monthly fee falls in a price range (BETWEEN). */
    public List<Room> findByFeeRange(double min, double max) throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT *, (base_monthly_fee * " +
                "CASE room_type WHEN 'SINGLE' THEN 1.60 WHEN 'DOUBLE' THEN 1.30 " +
                "WHEN 'TRIPLE' THEN 1.15 ELSE 1.00 END) AS computed_fee " +
                "FROM rooms HAVING computed_fee BETWEEN ? AND ? ORDER BY computed_fee";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, min);
            ps.setDouble(2, max);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(buildRoom(rs));
            }
        }
        return list;
    }

    public List<Room> findRoomsWithFreeBeds() throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE occupied_beds < capacity AND status <> 'MAINTENANCE'";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(buildRoom(rs));
        }
        return list;
    }

    @Override
    public List<Room> searchByKeyword(String keyword) throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE room_number LIKE ? OR block LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(buildRoom(rs));
            }
        }
        return list;
    }

    /** Distinct room types currently in the system (HashSet demo). */
    public Set<String> getUniqueRoomTypes() throws SQLException {
        Set<String> types = new HashSet<>();
        String sql = "SELECT DISTINCT room_type FROM rooms";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) types.add(rs.getString("room_type"));
        }
        return types;
    }

    /** Revenue potential by room type using LEFT JOIN across allocations+payments. */
    public void printRevenueByRoomType() throws SQLException {
        String sql = "SELECT r.room_type, COALESCE(SUM(p.amount),0) AS revenue " +
                "FROM rooms r " +
                "LEFT JOIN allocations a ON r.room_id = a.room_id " +
                "LEFT JOIN payments p ON a.allocation_id = p.allocation_id " +
                "GROUP BY r.room_type ORDER BY revenue DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.printf("  %-12s : Rs.%.2f%n", rs.getString("room_type"), rs.getDouble("revenue"));
            }
        }
    }

    /** Factory method: builds the correct Room subclass instance from a DB row. */
    private Room buildRoom(ResultSet rs) throws SQLException {
        String type = rs.getString("room_type");
        int roomId = rs.getInt("room_id");
        String roomNumber = rs.getString("room_number");
        String block = rs.getString("block");
        int floor = rs.getInt("floor");
        double baseFee = rs.getDouble("base_monthly_fee");
        String status = rs.getString("status");
        int flag = rs.getInt("feature_flag");

        Room room;
        switch (type) {
            case "SINGLE":
                room = new SingleRoom(roomId, roomNumber, block, floor, baseFee, status, flag == 1);
                break;
            case "DOUBLE":
                room = new DoubleRoom(roomId, roomNumber, block, floor, baseFee, status, flag == 1);
                break;
            case "TRIPLE":
                room = new TripleRoom(roomId, roomNumber, block, floor, baseFee, status, flag == 1);
                break;
            case "DORMITORY":
                room = new DormitoryRoom(roomId, roomNumber, block, floor, baseFee, status, flag);
                break;
            default:
                throw new SQLException("Unknown room type in database: " + type);
        }
        room.setOccupiedBeds(rs.getInt("occupied_beds"));
        return room;
    }

    /** Extracts the subclass-specific boolean/int flag for storage. */
    private int extractFeatureFlag(Room r) {
        if (r instanceof SingleRoom) return ((SingleRoom) r).isHasAttachedBathroom() ? 1 : 0;
        if (r instanceof DoubleRoom) return ((DoubleRoom) r).isHasBalcony() ? 1 : 0;
        if (r instanceof TripleRoom) return ((TripleRoom) r).isHasStudyLounge() ? 1 : 0;
        if (r instanceof DormitoryRoom) return ((DormitoryRoom) r).getLockerCount();
        return 0;
    }
}
