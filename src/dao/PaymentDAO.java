package dao;

import model.FeePayment;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Data Access Object for FeePayment records and revenue analytics. */
public class PaymentDAO {

    public int savePayment(FeePayment payment) throws SQLException {
        String sql = "INSERT INTO payments (allocation_id, amount, mode, transaction_reference, payment_date) " +
                "VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getAllocationId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getMode().name());
            ps.setString(4, payment.getTransactionReference());
            ps.setDate(5, Date.valueOf(payment.getPaymentDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<FeePayment> findByAllocation(int allocationId) throws SQLException {
        List<FeePayment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE allocation_id=? ORDER BY payment_date";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, allocationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public double getTotalPaid(int allocationId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) AS total FROM payments WHERE allocation_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, allocationId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble("total");
            }
        }
    }

    /** Revenue grouped by payment mode using CASE + GROUP BY. */
    public void printRevenueByMode() throws SQLException {
        String sql = "SELECT mode, SUM(amount) AS total, " +
                "CASE mode WHEN 'CASH' THEN 'Walk-in Payment' " +
                "WHEN 'CARD' THEN 'Card Terminal' " +
                "WHEN 'UPI' THEN 'UPI QR' ELSE 'Bank Transfer' END AS description " +
                "FROM payments GROUP BY mode ORDER BY total DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.printf("  %-6s (%-16s) : Rs.%.2f%n",
                        rs.getString("mode"), rs.getString("description"), rs.getDouble("total"));
            }
        }
    }

    /** Total hostel revenue collected within a date range. */
    public double getRevenueBetween(LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) AS total FROM payments WHERE payment_date BETWEEN ? AND ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble("total");
            }
        }
    }

    private FeePayment mapRow(ResultSet rs) throws SQLException {
        return new FeePayment(
                rs.getInt("payment_id"),
                rs.getInt("allocation_id"),
                rs.getDouble("amount"),
                FeePayment.Mode.valueOf(rs.getString("mode")),
                rs.getString("transaction_reference"),
                rs.getDate("payment_date").toLocalDate()
        );
    }
}
