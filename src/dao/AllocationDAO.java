package dao;

import interfaces.Searchable;
import model.Allocation;
import model.Room;
import model.Student;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Allocation. Contains the core transactional
 * "allocate bed" and "vacate bed" operations that must keep the
 * allocations and rooms tables consistent.
 */
public class AllocationDAO implements Searchable<Allocation> {

    private final StudentDAO studentDAO = new StudentDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    /** Returns true if the given room currently has an active allocation on that bed number. */
    public boolean isBedOccupied(int roomId, int bedNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM allocations " +
                "WHERE room_id = ? AND bed_number = ? AND status = 'ACTIVE'";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, bedNumber);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Transactionally creates an allocation AND increments the room's occupied_beds count.
     * Both operations commit together or roll back together (ACID).
     */
    public int allocate(int studentId, int roomId, int bedNumber, LocalDate allocationDate,
                         String academicYear) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        String insertSql = "INSERT INTO allocations (student_id, room_id, bed_number, " +
                "allocation_date, academic_year, status) VALUES (?,?,?,?,?,'ACTIVE')";
        String updateRoomSql = "UPDATE rooms SET occupied_beds = occupied_beds + 1, " +
                "status = CASE WHEN occupied_beds + 1 >= capacity THEN 'FULL' ELSE 'AVAILABLE' END " +
                "WHERE room_id = ?";
        int newId = -1;
        try (PreparedStatement ps1 = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement ps2 = conn.prepareStatement(updateRoomSql)) {

            ps1.setInt(1, studentId);
            ps1.setInt(2, roomId);
            ps1.setInt(3, bedNumber);
            ps1.setDate(4, Date.valueOf(allocationDate));
            ps1.setString(5, academicYear);
            ps1.executeUpdate();
            try (ResultSet keys = ps1.getGeneratedKeys()) {
                if (keys.next()) newId = keys.getInt(1);
            }

            ps2.setInt(1, roomId);
            ps2.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
        return newId;
    }

    /** Transactionally vacates a bed: marks allocation VACATED and frees the room bed. */
    public boolean vacate(int allocationId, LocalDate vacateDate) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        String selectSql = "SELECT room_id FROM allocations WHERE allocation_id=? AND status='ACTIVE'";
        String updateAllocSql = "UPDATE allocations SET status='VACATED', vacate_date=? WHERE allocation_id=?";
        String updateRoomSql = "UPDATE rooms SET occupied_beds = GREATEST(occupied_beds - 1, 0), " +
                "status = 'AVAILABLE' WHERE room_id = ?";
        try (PreparedStatement psSel = conn.prepareStatement(selectSql)) {
            psSel.setInt(1, allocationId);
            int roomId;
            try (ResultSet rs = psSel.executeQuery()) {
                if (!rs.next()) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }
                roomId = rs.getInt("room_id");
            }
            try (PreparedStatement ps1 = conn.prepareStatement(updateAllocSql);
                 PreparedStatement ps2 = conn.prepareStatement(updateRoomSql)) {
                ps1.setDate(1, Date.valueOf(vacateDate));
                ps1.setInt(2, allocationId);
                ps1.executeUpdate();

                ps2.setInt(1, roomId);
                ps2.executeUpdate();

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    @Override
    public Allocation findById(int id) throws SQLException {
        String sql = "SELECT * FROM allocations WHERE allocation_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Allocation> findAll() throws SQLException {
        List<Allocation> list = new ArrayList<>();
        String sql = "SELECT * FROM allocations ORDER BY allocation_id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Allocation> findByStudent(int studentId) throws SQLException {
        List<Allocation> list = new ArrayList<>();
        String sql = "SELECT * FROM allocations WHERE student_id=? ORDER BY allocation_date DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Allocations that started within a given date range (BETWEEN). */
    public List<Allocation> findByDateRange(LocalDate start, LocalDate end) throws SQLException {
        List<Allocation> list = new ArrayList<>();
        String sql = "SELECT * FROM allocations WHERE allocation_date BETWEEN ? AND ? ORDER BY allocation_date";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<Allocation> searchByKeyword(String keyword) throws SQLException {
        List<Allocation> list = new ArrayList<>();
        String sql = "SELECT a.* FROM allocations a JOIN students s ON a.student_id = s.student_id " +
                "WHERE s.name LIKE ? OR s.roll_number LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Allocation mapRow(ResultSet rs) throws SQLException {
        Student student = studentDAO.findById(rs.getInt("student_id"));
        Room room = roomDAO.findById(rs.getInt("room_id"));
        Date vacateDate = rs.getDate("vacate_date");
        return new Allocation(
                rs.getInt("allocation_id"),
                student,
                room,
                rs.getInt("bed_number"),
                rs.getDate("allocation_date").toLocalDate(),
                vacateDate == null ? null : vacateDate.toLocalDate(),
                rs.getString("academic_year"),
                rs.getString("status")
        );
    }
}
