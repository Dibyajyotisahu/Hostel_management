package dao;

import interfaces.Searchable;
import model.Student;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student entity.
 * Contains only SQL/JDBC logic - no business rules (DAO pattern).
 */
public class StudentDAO implements Searchable<Student> {

    public int insert(Student s) throws SQLException {
        String sql = "INSERT INTO students (name, roll_number, course, year, phone, email, " +
                "guardian_name, guardian_phone, address, id_proof_type, id_proof_number, admission_date) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getRollNumber());
            ps.setString(3, s.getCourse());
            ps.setInt(4, s.getYear());
            ps.setString(5, s.getPhone());
            ps.setString(6, s.getEmail());
            ps.setString(7, s.getGuardianName());
            ps.setString(8, s.getGuardianPhone());
            ps.setString(9, s.getAddress());
            ps.setString(10, s.getIdProofType());
            ps.setString(11, s.getIdProofNumber());
            ps.setDate(12, Date.valueOf(s.getAdmissionDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean update(Student s) throws SQLException {
        String sql = "UPDATE students SET name=?, roll_number=?, course=?, year=?, phone=?, email=?, " +
                "guardian_name=?, guardian_phone=?, address=?, id_proof_type=?, id_proof_number=? WHERE student_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getRollNumber());
            ps.setString(3, s.getCourse());
            ps.setInt(4, s.getYear());
            ps.setString(5, s.getPhone());
            ps.setString(6, s.getEmail());
            ps.setString(7, s.getGuardianName());
            ps.setString(8, s.getGuardianPhone());
            ps.setString(9, s.getAddress());
            ps.setString(10, s.getIdProofType());
            ps.setString(11, s.getIdProofNumber());
            ps.setInt(12, s.getStudentId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Student findById(int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Student> findAll() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY student_id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<Student> searchByKeyword(String keyword) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE name LIKE ? OR roll_number LIKE ? OR phone LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Students who currently owe more in fees than they've paid (subquery). */
    public List<Student> findStudentsWithDues(double threshold) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE student_id IN (" +
                "  SELECT a.student_id FROM allocations a " +
                "  LEFT JOIN payments p ON a.allocation_id = p.allocation_id " +
                "  GROUP BY a.student_id " +
                "  HAVING COALESCE(SUM(p.amount), 0) < ? )";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Count of students per course (GROUP BY). */
    public void printStudentCountByCourse() throws SQLException {
        String sql = "SELECT course, COUNT(*) AS total FROM students GROUP BY course ORDER BY total DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.printf("  %-15s : %d students%n", rs.getString("course"), rs.getInt("total"));
            }
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("student_id"),
                rs.getString("name"),
                rs.getString("roll_number"),
                rs.getString("course"),
                rs.getInt("year"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("guardian_name"),
                rs.getString("guardian_phone"),
                rs.getString("address"),
                rs.getString("id_proof_type"),
                rs.getString("id_proof_number"),
                rs.getDate("admission_date").toLocalDate()
        );
    }
}
